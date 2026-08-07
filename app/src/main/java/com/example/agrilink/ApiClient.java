package com.example.agrilink;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AgriLink AI — Android API Client Helper (com.example.agrilink)
 * Connects Android Studio App to FastAPI Backend at http://10.0.2.2:8080/api/v1
 */
public class ApiClient {

    private static final String TAG = "ApiClient";
    // 10.32.73.166 for physical Android phone on Wi-Fi (use 10.0.2.2 for Android Studio Emulator)
    public static final String BASE_URL = "http://10.32.73.166:8080/api/v1";
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback {
        void onSuccess(String response, int statusCode);
        void onError(Exception e);
    }

    public static void get(String endpoint, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                
                String responseText = readStream(is);
                mainHandler.post(() -> callback.onSuccess(responseText, responseCode));
            } catch (Exception e) {
                Log.e(TAG, "GET Request Error: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public static void post(String endpoint, String jsonBody, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                
                String responseText = readStream(is);
                mainHandler.post(() -> callback.onSuccess(responseText, responseCode));
            } catch (Exception e) {
                Log.e(TAG, "POST Request Error: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private static String readStream(InputStream stream) throws Exception {
        if (stream == null) return "";
        BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        return response.toString();
    }
}
