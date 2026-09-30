package com.agrilink.app.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.BufferedReader
import java.io.Closeable
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Locale
import kotlin.math.exp

/**
 * PlantDiseaseClassifier loads a local TensorFlow Lite model and class index mappings
 * to classify crop leaf diseases.
 *
 * Preprocessing:
 * - Resizes input image to 384x384.
 * - Normalizes pixel values into Float32 tensor.
 * - Executes inference using TFLite Interpreter.
 * - Formats plant disease name, confidence percentage, and recommended remedies.
 */
class PlantDiseaseClassifier @JvmOverloads constructor(
    private val context: Context,
    private val modelFileName: String = DEFAULT_MODEL_NAME,
    private val labelsFileName: String = DEFAULT_LABELS_NAME
) : Closeable {

    companion object {
        private const val TAG = "PlantDiseaseClassifier"
        const val DEFAULT_MODEL_NAME = "model_float16_quant.tflite"
        const val FALLBACK_MODEL_NAME = "model_float16_quant (1).tflite"
        const val DEFAULT_LABELS_NAME = "class_indices.json"
        const val INPUT_SIZE = 384
        const val NUM_CLASSES = 38
    }

    /**
     * Prediction result container.
     */
    data class Recognition(
        val classId: Int,
        val rawLabel: String,
        val plantName: String,
        val diseaseName: String,
        val confidence: Float,
        val confidencePercentage: String,
        val isHealthy: Boolean,
        val remedyAdvice: String
    ) {
        val displayName: String
            get() = if (isHealthy) "$plantName — Healthy" else "$plantName — $diseaseName"

        val formattedResult: String
            get() = "$displayName ($confidencePercentage)"
    }

    private var interpreter: Interpreter? = null
    private val labelMap = mutableMapOf<Int, String>()

    // Standard ImageProcessor with 384x384 bilinear resize and normalization (0..255 -> 0.0..1.0)
    private val standardNormalizedProcessor: ImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f)) // Normalize [0, 255] to [0.0, 1.0]
        .build()

    // Pass-through Float32 ImageProcessor (0..255 float range for models with internal Rescaling layer)
    private val floatRangeProcessor: ImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 1f)) // Converts to Float32 keeping [0.0, 255.0]
        .build()

    init {
        loadLabels()
        loadInterpreter()
    }

    /**
     * Loads class labels from class_indices.json in assets.
     */
    private fun loadLabels() {
        try {
            val jsonString = context.assets.open(labelsFileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).readText()
            }
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val index = key.toIntOrNull()
                val label = jsonObject.getString(key)
                if (index != null) {
                    labelMap[index] = label
                }
            }
            Log.d(TAG, "Successfully loaded ${labelMap.size} plant disease class labels.")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels from $labelsFileName", e)
            loadFallbackLabels()
        }
    }

    /**
     * Loads the TFLite model from assets.
     */
    private fun loadInterpreter() {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }

        var byteBuffer: ByteBuffer? = null

        // Try primary model file name
        try {
            byteBuffer = loadModelFile(modelFileName)
            Log.d(TAG, "Loaded model buffer from $modelFileName")
        } catch (e: Exception) {
            Log.w(TAG, "Could not load $modelFileName, trying fallback: $FALLBACK_MODEL_NAME", e)
            try {
                byteBuffer = loadModelFile(FALLBACK_MODEL_NAME)
                Log.d(TAG, "Loaded model buffer from fallback $FALLBACK_MODEL_NAME")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to load fallback model as well", e2)
            }
        }

        byteBuffer?.let {
            interpreter = Interpreter(it, options)
            Log.d(TAG, "TFLite Interpreter initialized successfully.")
        } ?: run {
            Log.e(TAG, "TFLite Interpreter could not be created: model buffer is null.")
        }
    }

    @Throws(Exception::class)
    private fun loadModelFile(fileName: String): ByteBuffer {
        return try {
            FileUtil.loadMappedFile(context, fileName)
        } catch (e: Exception) {
            val fileDescriptor: AssetFileDescriptor = context.assets.openFd(fileName)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    /**
     * Executes inference on the provided Bitmap.
     * Preprocesses the image to 384x384 with normalization, runs the model,
     * and returns the top prediction.
     */
    @Synchronized
    fun classify(bitmap: Bitmap): Recognition {
        val interp = interpreter ?: throw IllegalStateException("TFLite Interpreter is not initialized.")

        // Run inference with standard normalization ([0, 1])
        val rec1 = runInferenceWithProcessor(bitmap, standardNormalizedProcessor, interp)

        // Run inference with [0, 255] float range (for models containing internal Rescaling(1/128, -1))
        val rec2 = runInferenceWithProcessor(bitmap, floatRangeProcessor, interp)

        // Return the prediction that yields higher confidence
        val best = if (rec2.confidence >= rec1.confidence) rec2 else rec1
        Log.d(TAG, "Classification result: ${best.displayName} with confidence ${best.confidencePercentage}")
        return best
    }

    /**
     * Classifies and returns top K predictions.
     */
    @Synchronized
    fun classifyTopK(bitmap: Bitmap, k: Int = 3): List<Recognition> {
        val interp = interpreter ?: throw IllegalStateException("TFLite Interpreter is not initialized.")
        val list1 = runInferenceTopK(bitmap, standardNormalizedProcessor, interp, k)
        val list2 = runInferenceTopK(bitmap, floatRangeProcessor, interp, k)

        val top1 = list1.firstOrNull()?.confidence ?: 0f
        val top2 = list2.firstOrNull()?.confidence ?: 0f

        return if (top2 >= top1) list2 else list1
    }

    private fun runInferenceWithProcessor(
        bitmap: Bitmap,
        processor: ImageProcessor,
        interp: Interpreter
    ): Recognition {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val processedImage = processor.process(tensorImage)

        val output = Array(1) { FloatArray(NUM_CLASSES) }
        interp.run(processedImage.buffer, output)

        val probabilities = ensureProbabilities(output[0])
        var maxIndex = 0
        var maxScore = -1f

        for (i in probabilities.indices) {
            if (probabilities[i] > maxScore) {
                maxScore = probabilities[i]
                maxIndex = i
            }
        }

        return createRecognition(maxIndex, maxScore)
    }

    private fun runInferenceTopK(
        bitmap: Bitmap,
        processor: ImageProcessor,
        interp: Interpreter,
        k: Int
    ): List<Recognition> {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val processedImage = processor.process(tensorImage)

        val output = Array(1) { FloatArray(NUM_CLASSES) }
        interp.run(processedImage.buffer, output)

        val probabilities = ensureProbabilities(output[0])

        return probabilities.indices
            .map { idx -> createRecognition(idx, probabilities[idx]) }
            .sortedByDescending { it.confidence }
            .take(k)
    }

    /**
     * Ensures output scores are valid softmax probabilities summing to 1.0.
     */
    private fun ensureProbabilities(scores: FloatArray): FloatArray {
        val sum = scores.sum()
        val allInRange = scores.all { it in 0f..1f }
        if (allInRange && kotlin.math.abs(sum - 1.0f) < 0.05f) {
            return scores
        }

        // Apply numerically stable softmax
        var maxVal = Float.NEGATIVE_INFINITY
        for (v in scores) {
            if (v > maxVal) maxVal = v
        }

        val expValues = FloatArray(scores.size)
        var expSum = 0f
        for (i in scores.indices) {
            val expVal = exp(scores[i] - maxVal)
            expValues[i] = expVal
            expSum += expVal
        }

        if (expSum > 0f) {
            for (i in expValues.indices) {
                expValues[i] = expValues[i] / expSum
            }
        }
        return expValues
    }

    private fun createRecognition(classId: Int, confidence: Float): Recognition {
        val rawLabel = labelMap[classId] ?: "Unknown___Unknown"
        val (plant, disease) = parseLabel(rawLabel)
        val isHealthy = disease.equals("healthy", ignoreCase = true) || rawLabel.contains("healthy", ignoreCase = true)
        val confidencePct = String.format(Locale.US, "%.1f%%", confidence * 100)
        val remedy = getRemedyForDisease(plant, disease, isHealthy)

        return Recognition(
            classId = classId,
            rawLabel = rawLabel,
            plantName = plant,
            diseaseName = if (isHealthy) "Healthy" else disease,
            confidence = confidence,
            confidencePercentage = confidencePct,
            isHealthy = isHealthy,
            remedyAdvice = remedy
        )
    }

    private fun parseLabel(rawLabel: String): Pair<String, String> {
        val parts = rawLabel.split("___")
        val rawPlant = parts.getOrNull(0) ?: "Plant"
        val rawDisease = parts.getOrNull(1) ?: "Disease"

        val cleanPlant = rawPlant.replace("_", " ").trim()
        val cleanDisease = rawDisease.replace("_", " ").trim()

        return Pair(cleanPlant, cleanDisease)
    }

    /**
     * Provides agronomic chemical treatment & cultural practices for each disease class.
     */
    private fun getRemedyForDisease(plant: String, disease: String, isHealthy: Boolean): String {
        if (isHealthy) {
            return "🌿 Healthy $plant Crop: No chemical treatment needed. Maintain optimal irrigation schedule, balanced N-P-K fertilization, and inspect weekly for early signs of pests."
        }

        val lowerDisease = disease.lowercase(Locale.ROOT)

        return when {
            lowerDisease.contains("early blight") ->
                "🧪 Spray Mancozeb 75% WP @ 2.5g/L or Copper Oxychloride 50% WP @ 3g/L. Prune infected bottom foliage and avoid overhead watering."
            lowerDisease.contains("late blight") ->
                "🧪 Spray Metalaxyl 8% + Mancozeb 64% WP (Ridomil Gold) @ 2g/L or Dimethomorph 50% WP @ 1g/L. Ensure strict field drainage."
            lowerDisease.contains("leaf mold") ->
                "🧪 Spray Carbendazim 50% WP @ 1g/L or Difenoconazole 25% EC @ 0.5ml/L. Increase spacing between rows to enhance air flow."
            lowerDisease.contains("septoria") ->
                "🧪 Spray Chlorothalonil 75% WP @ 2g/L or Azoxystrobin 23% SC @ 1ml/L. Remove crop debris after harvesting."
            lowerDisease.contains("spider mite") || lowerDisease.contains("two-spotted") ->
                "🧪 Spray Abamectin 1.9% EC @ 0.5ml/L or Propargite 57% EC @ 2ml/L. Keep soil adequately moist as mites thrive in hot dry weather."
            lowerDisease.contains("target spot") ->
                "🧪 Spray Pyraclostrobin 20% WG @ 1g/L or Mancozeb 75% WP @ 2.5g/L. Practice 3-year crop rotation."
            lowerDisease.contains("yellow leaf curl") || lowerDisease.contains("curl virus") ->
                "🧪 Vector Control (Whiteflies): Spray Imidacloprid 17.8% SL @ 0.5ml/L or Acetamiprid 20% SP @ 0.2g/L. Install yellow sticky traps."
            lowerDisease.contains("mosaic virus") ->
                "🧪 Vector Control (Aphids): Spray Thiamethoxam 25% WG @ 0.3g/L. Disinfect farm tools in 10% bleach solution and remove infected plants."
            lowerDisease.contains("bacterial spot") ->
                "🧪 Spray Streptocycline 90:10 (1g in 10L water) + Copper Hydroxide 53.8% DF @ 2g/L. Avoid handling wet foliage."
            lowerDisease.contains("powdery mildew") ->
                "🧪 Spray Hexaconazole 5% EC @ 1ml/L or Wettable Sulphur 80% WP @ 3g/L. Ensure adequate sunlight penetration."
            lowerDisease.contains("apple scab") ->
                "🧪 Spray Captan 50% WP @ 2.5g/L or Myclobutanil 10% WP @ 0.5g/L during early spring pink bud stage."
            lowerDisease.contains("black rot") ->
                "🧪 Spray Tebuconazole 25.9% EC @ 1ml/L or Copper Oxychloride 50% WP @ 3g/L. Prune infected mummified fruit."
            lowerDisease.contains("cedar apple rust") ->
                "🧪 Spray Mancozeb 75% WP @ 2.5g/L or Trifloxystrobin @ 0.5g/L when buds show green tip."
            lowerDisease.contains("common rust") || lowerDisease.contains("rust") ->
                "🧪 Spray Propiconazole 25% EC @ 1ml/L or Mancozeb 75% WP @ 2.5g/L upon first appearance of pustules."
            lowerDisease.contains("gray leaf spot") || lowerDisease.contains("cercospora") ->
                "🧪 Spray Azoxystrobin + Difenoconazole @ 1ml/L. Practice deep tillage to bury crop debris."
            lowerDisease.contains("northern leaf blight") ->
                "🧪 Spray Mancozeb 75% WP @ 2.5g/L or Azoxystrobin @ 1ml/L. Use resistant hybrid varieties."
            lowerDisease.contains("haunglongbing") || lowerDisease.contains("greening") ->
                "🧪 Citrus Greening: Control Citrus Psyllid vectors using Dimethoate 30% EC @ 1.5ml/L or Imidacloprid. Apply micronutrients (Zinc & Iron)."
            lowerDisease.contains("esca") || lowerDisease.contains("black measles") ->
                "🧪 Prune infected wood 10cm below symptomatic areas. Paint wounds with fungicidal paste (Copper paste)."
            lowerDisease.contains("leaf scorch") ->
                "🧪 Spray Copper Oxychloride 50% WP @ 2.5g/L or Benomyl 50% WP @ 1g/L. Maintain balanced potassium levels."
            else ->
                "🧪 General Advisory for $plant: Spray broad-spectrum bio-fungicide (Trichoderma viride @ 5g/L) or Neem Oil 10,000 PPM @ 3ml/L. Remove severely damaged leaves to curb spreading."
        }
    }

    private fun loadFallbackLabels() {
        val defaults = arrayOf(
            "Apple___Apple_scab", "Apple___Black_rot", "Apple___Cedar_apple_rust", "Apple___healthy",
            "Blueberry___healthy", "Cherry___Powdery_mildew", "Cherry___healthy",
            "Corn___Cercospora_leaf_spot Gray_leaf_spot", "Corn___Common_rust", "Corn___Northern_Leaf_Blight",
            "Corn___healthy", "Grape___Black_rot", "Grape___Esca_(Black_Measles)",
            "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)", "Grape___healthy",
            "Orange___Haunglongbing_(Citrus_greening)", "Peach___Bacterial_spot", "Peach___healthy",
            "Pepper,_bell___Bacterial_spot", "Pepper,_bell___healthy", "Potato___Early_blight",
            "Potato___Late_blight", "Potato___healthy", "Raspberry___healthy", "Soybean___healthy",
            "Squash___Powdery_mildew", "Strawberry___Leaf_scorch", "Strawberry___healthy",
            "Tomato___Bacterial_spot", "Tomato___Early_blight", "Tomato___Late_blight",
            "Tomato___Leaf_Mold", "Tomato___Septoria_leaf_spot",
            "Tomato___Spider_mites Two-spotted_spider_mite", "Tomato___Target_Spot",
            "Tomato___Tomato_Yellow_Leaf_Curl_Virus", "Tomato___Tomato_mosaic_virus", "Tomato___healthy"
        )
        for (i in defaults.indices) {
            labelMap[i] = defaults[i]
        }
    }

    override fun close() {
        try {
            interpreter?.close()
            interpreter = null
            Log.d(TAG, "TFLite Interpreter closed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing TFLite Interpreter", e)
        }
    }
}
