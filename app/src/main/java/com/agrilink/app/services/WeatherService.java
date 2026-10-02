package com.agrilink.app.services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * KrishiSetu Real-Time Live Weather Service
 * Powered by Open-Meteo Global Forecasting API (No API Key Required)
 */
public class WeatherService {

    private static final String TAG = "WeatherService";
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Pre-mapped Mandi / Regional Coordinates for ultra-fast instant resolution
    private static final Map<String, double[]> KNOWN_COORDINATES = new HashMap<>();

    static {
        KNOWN_COORDINATES.put("indore", new double[]{22.7196, 75.8577});
        KNOWN_COORDINATES.put("dewas", new double[]{22.9658, 76.0553});
        KNOWN_COORDINATES.put("ujjain", new double[]{23.1765, 75.7885});
        KNOWN_COORDINATES.put("pithampur", new double[]{22.6100, 75.6800});
        KNOWN_COORDINATES.put("bhopal", new double[]{23.2599, 77.4126});
        KNOWN_COORDINATES.put("karnal", new double[]{29.6857, 76.9905});
        KNOWN_COORDINATES.put("ludhiana", new double[]{30.9010, 75.8573});
        KNOWN_COORDINATES.put("delhi", new double[]{28.6139, 77.2090});
        KNOWN_COORDINATES.put("jaipur", new double[]{26.9124, 75.7873});
        KNOWN_COORDINATES.put("jodhpur", new double[]{26.2389, 73.0243});
        KNOWN_COORDINATES.put("kota", new double[]{25.2138, 75.8648});
        KNOWN_COORDINATES.put("udaipur", new double[]{24.5854, 73.7125});
        KNOWN_COORDINATES.put("chandigarh", new double[]{30.7333, 76.7794});
        KNOWN_COORDINATES.put("lucknow", new double[]{26.8467, 80.9462});
        KNOWN_COORDINATES.put("ahmedabad", new double[]{23.0225, 72.5714});
        KNOWN_COORDINATES.put("pune", new double[]{18.5204, 73.8567});
        KNOWN_COORDINATES.put("nagpur", new double[]{21.1458, 79.0882});
    }

    public static class WeatherData {
        public String locationName;
        public double temperature;
        public double apparentTemperature;
        public int humidity;
        public double windSpeed;
        public int weatherCode;
        public String conditionEn;
        public String conditionHi;
        public String advisoryEn;
        public String advisoryHi;
        public String emoji;
        public boolean isLive;

        public String getFormattedTemp() {
            return Math.round(temperature) + "°C";
        }

        public String getCondition(boolean isHindi) {
            return isHindi ? conditionHi : conditionEn;
        }

        public String getAdvisory(boolean isHindi) {
            return isHindi ? advisoryHi : advisoryEn;
        }
    }

    public interface WeatherCallback {
        void onSuccess(WeatherData data);
        void onError(String error);
    }

    /**
     * Fetch live weather for a city or Mandi name.
     */
    public static void fetchWeatherForCity(String cityName, WeatherCallback callback) {
        executor.execute(() -> {
            try {
                String cleanName = cleanCityName(cityName);
                double[] coords = resolveCoordinates(cleanName);

                if (coords != null) {
                    executeWeatherFetch(coords[0], coords[1], cityName, callback);
                } else {
                    // Geocode via Open-Meteo Geocoding API
                    double[] geoCoords = geocodeCity(cleanName);
                    if (geoCoords != null) {
                        executeWeatherFetch(geoCoords[0], geoCoords[1], cityName, callback);
                    } else {
                        // Default fallback to Indore
                        executeWeatherFetch(22.7196, 75.8577, cityName, callback);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather for city: " + cityName, e);
                mainHandler.post(() -> callback.onError("Failed to fetch weather: " + e.getMessage()));
            }
        });
    }

    public static class ResolvedLocation {
        public final double latitude;
        public final double longitude;
        public final String locationName;

        public ResolvedLocation(double latitude, double longitude, String locationName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.locationName = locationName;
        }
    }

    /**
     * Fetch live weather using device coordinates (GPS / Network / IP) and reverse-geocode to real city & state.
     */
    public static void fetchWeatherForGps(Context context, WeatherCallback callback) {
        executor.execute(() -> {
            try {
                Location deviceLoc = getDeviceLocation(context);
                double lat = 0;
                double lon = 0;
                String resolvedName = null;

                if (deviceLoc != null) {
                    lat = deviceLoc.getLatitude();
                    lon = deviceLoc.getLongitude();
                    resolvedName = reverseGeocode(context, lat, lon);
                }

                // If GPS hardware is indoors or not returning a fix, try fast IP-based geolocation
                if (lat == 0 && lon == 0) {
                    ResolvedLocation ipLoc = fetchLocationFromIP();
                    if (ipLoc != null) {
                        lat = ipLoc.latitude;
                        lon = ipLoc.longitude;
                        if (resolvedName == null || resolvedName.trim().isEmpty()) {
                            resolvedName = ipLoc.locationName;
                        }
                    }
                }

                // If coordinates were found, resolve name if needed and fetch weather
                if (lat != 0 && lon != 0) {
                    if (resolvedName == null || resolvedName.trim().isEmpty()) {
                        resolvedName = reverseGeocode(context, lat, lon);
                    }
                    String finalName = (resolvedName != null && !resolvedName.trim().isEmpty()) ?
                            resolvedName : "Live GPS (" + String.format(Locale.US, "%.2f, %.2f", lat, lon) + ")";
                    executeWeatherFetch(lat, lon, finalName, callback);
                } else {
                    // Fallback to Jaipur Mandi, Rajasthan
                    executeWeatherFetch(26.9124, 75.7873, "Jaipur Mandi, Rajasthan", callback);
                }
            } catch (Exception e) {
                Log.e(TAG, "GPS weather fetch error", e);
                mainHandler.post(() -> callback.onError("Could not detect location. Please check GPS settings."));
            }
        });
    }

    /**
     * Fetch live weather for explicit GPS coordinates.
     */
    public static void fetchWeatherForCoordinates(Context context, double lat, double lon, WeatherCallback callback) {
        executor.execute(() -> {
            try {
                String resolvedName = reverseGeocode(context, lat, lon);
                String finalName = (resolvedName != null && !resolvedName.trim().isEmpty()) ?
                        resolvedName : "Live GPS (" + String.format(Locale.US, "%.2f, %.2f", lat, lon) + ")";
                executeWeatherFetch(lat, lon, finalName, callback);
            } catch (Exception e) {
                Log.e(TAG, "fetchWeatherForCoordinates error", e);
                executeWeatherFetch(lat, lon, "Live GPS Location", callback);
            }
        });
    }

    private static Location getDeviceLocation(Context context) {
        if (context == null) return null;
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return null;

            Location bestLocation = null;

            // 1. Check enabled providers for last known location
            List<String> providers = lm.getProviders(true);
            if (providers != null) {
                for (String provider : providers) {
                    try {
                        Location l = lm.getLastKnownLocation(provider);
                        if (l != null) {
                            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                                bestLocation = l;
                            }
                        }
                    } catch (SecurityException ignored) {}
                }
            }

            // 2. Also check standard providers
            if (bestLocation == null) {
                try {
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        bestLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                } catch (SecurityException ignored) {}
            }
            if (bestLocation == null) {
                try {
                    if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        bestLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                } catch (SecurityException ignored) {}
            }

            // If we have a recent location (within 20 minutes), use it immediately
            if (bestLocation != null && (System.currentTimeMillis() - bestLocation.getTime() < 20 * 60 * 1000)) {
                return bestLocation;
            }

            // 3. Otherwise request an active location update with a 3.5s timeout
            final Location[] activeLoc = new Location[1];
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

            android.location.LocationListener listener = new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        activeLoc[0] = location;
                        try {
                            lm.removeUpdates(this);
                        } catch (SecurityException ignored) {}
                        latch.countDown();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            mainHandler.post(() -> {
                try {
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener, Looper.getMainLooper());
                    }
                    if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener, Looper.getMainLooper());
                    }
                } catch (SecurityException ignored) {
                    latch.countDown();
                } catch (Exception ignored) {
                    latch.countDown();
                }
            });

            try {
                latch.await(3500, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {}

            mainHandler.post(() -> {
                try {
                    lm.removeUpdates(listener);
                } catch (Exception ignored) {}
            });

            if (activeLoc[0] != null) {
                return activeLoc[0];
            }

            return bestLocation;
        } catch (SecurityException se) {
            Log.w(TAG, "Location permission not granted: " + se.getMessage());
        } catch (Exception e) {
            Log.w(TAG, "Could not obtain device location: " + e.getMessage());
        }
        return null;
    }

    private static String reverseGeocode(Context context, double lat, double lon) {
        // 1. Android Native Geocoder
        try {
            if (context != null && Geocoder.isPresent()) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(lat, lon, 1);
                if (list != null && !list.isEmpty()) {
                    Address a = list.get(0);
                    String locality = a.getLocality();
                    String subAdmin = a.getSubAdminArea();
                    String admin = a.getAdminArea();

                    String city = locality != null ? locality : (subAdmin != null ? subAdmin : admin);
                    if (city != null && !city.trim().isEmpty()) {
                        if (admin != null && !city.equalsIgnoreCase(admin)) {
                            return city + ", " + admin;
                        }
                        return city;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Native Geocoder failed: " + e.getMessage());
        }

        // 2. OpenStreetMap / Nominatim Reverse Geocoding
        try {
            String urlStr = "https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lon + "&format=json";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "KrishiSetu-Android/1.0");
            conn.setConnectTimeout(3500);
            conn.setReadTimeout(3500);
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject obj = new JSONObject(sb.toString());
                JSONObject addr = obj.optJSONObject("address");
                if (addr != null) {
                    String city = addr.optString("city", addr.optString("town", addr.optString("village", addr.optString("county", ""))));
                    String state = addr.optString("state", "");
                    if (!city.isEmpty()) {
                        return !state.isEmpty() ? (city + ", " + state) : city;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Nominatim reverse geocode error: " + e.getMessage());
        }

        // 3. HTTP Reverse Geocode fallback (BigDataCloud client)
        try {
            String urlStr = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=" +
                    lat + "&longitude=" + lon + "&localityLanguage=en";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3500);
            conn.setReadTimeout(3500);
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject obj = new JSONObject(sb.toString());
                String city = obj.optString("city", obj.optString("locality", ""));
                String state = obj.optString("principalSubdivision", "");
                if (!city.isEmpty()) {
                    return !state.isEmpty() ? (city + ", " + state) : city;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "HTTP reverse geocode fallback error: " + e.getMessage());
        }

        return null;
    }

    private static ResolvedLocation fetchLocationFromIP() {
        // 1. ipwho.is (fast, HTTPS, free, accurate city/state/coords)
        try {
            URL url = new URL("https://ipwho.is/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "KrishiSetu-Android");
            conn.setConnectTimeout(3500);
            conn.setReadTimeout(3500);
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject obj = new JSONObject(sb.toString());
                if (obj.optBoolean("success", false) || obj.has("latitude")) {
                    double lat = obj.getDouble("latitude");
                    double lon = obj.getDouble("longitude");
                    String city = obj.optString("city", "");
                    String region = obj.optString("region", "");
                    String name = !city.isEmpty() ? (!region.isEmpty() ? city + ", " + region : city) : null;
                    return new ResolvedLocation(lat, lon, name);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "ipwho.is error: " + e.getMessage());
        }

        // 2. freeipapi.com
        try {
            URL url = new URL("https://freeipapi.com/api/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "KrishiSetu-Android");
            conn.setConnectTimeout(3500);
            conn.setReadTimeout(3500);
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject obj = new JSONObject(sb.toString());
                if (obj.has("latitude") && obj.has("longitude")) {
                    double lat = obj.getDouble("latitude");
                    double lon = obj.getDouble("longitude");
                    String city = obj.optString("cityName", "");
                    String region = obj.optString("regionName", "");
                    String name = !city.isEmpty() ? (!region.isEmpty() ? city + ", " + region : city) : null;
                    return new ResolvedLocation(lat, lon, name);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "freeipapi error: " + e.getMessage());
        }

        // 3. ip-api.com
        try {
            URL url = new URL("http://ip-api.com/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3500);
            conn.setReadTimeout(3500);
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject obj = new JSONObject(sb.toString());
                if ("success".equalsIgnoreCase(obj.optString("status", ""))) {
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    String city = obj.optString("city", "");
                    String region = obj.optString("regionName", "");
                    String name = !city.isEmpty() ? (!region.isEmpty() ? city + ", " + region : city) : null;
                    return new ResolvedLocation(lat, lon, name);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "ip-api error: " + e.getMessage());
        }

        return null;
    }

    private static void executeWeatherFetch(double lat, double lon, String locationName, WeatherCallback callback) {
        try {
            String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                    "&longitude=" + lon +
                    "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONObject current = root.getJSONObject("current");

                WeatherData data = new WeatherData();
                data.locationName = locationName;
                data.temperature = current.optDouble("temperature_2m", 30.0);
                data.apparentTemperature = current.optDouble("apparent_temperature", data.temperature);
                data.humidity = current.optInt("relative_humidity_2m", 40);
                data.windSpeed = current.optDouble("wind_speed_10m", 10.0);
                data.weatherCode = current.optInt("weather_code", 0);
                data.isLive = true;

                assignConditionAndAdvisory(data);

                mainHandler.post(() -> callback.onSuccess(data));
            } else {
                mainHandler.post(() -> callback.onError("Server returned status: " + responseCode));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing weather fetch: " + e.getMessage(), e);
            mainHandler.post(() -> callback.onError("Weather API connection failed: " + e.getMessage()));
        }
    }

    private static void assignConditionAndAdvisory(WeatherData data) {
        int code = data.weatherCode;
        double temp = data.temperature;
        double wind = data.windSpeed;
        int humidity = data.humidity;

        // Interpret WMO Weather Code
        if (code == 0) {
            data.emoji = "☀️";
            data.conditionEn = "Clear Sky & Sunny";
            data.conditionHi = "साफ़ आसमान और धूप";
        } else if (code == 1 || code == 2) {
            data.emoji = "⛅";
            data.conditionEn = "Partly Cloudy";
            data.conditionHi = "आंशिक रूप से बादल";
        } else if (code == 3) {
            data.emoji = "☁️";
            data.conditionEn = "Overcast";
            data.conditionHi = "बादल छाए रहेंगे";
        } else if (code >= 45 && code <= 48) {
            data.emoji = "🌫️";
            data.conditionEn = "Foggy";
            data.conditionHi = "धुंध और कोहरा";
        } else if (code >= 51 && code <= 67) {
            data.emoji = "🌧️";
            data.conditionEn = "Rain & Showers";
            data.conditionHi = "बारिश / फुहारें";
        } else if (code >= 71 && code <= 77) {
            data.emoji = "❄️";
            data.conditionEn = "Snow / Sleet";
            data.conditionHi = "बर्फबारी / ओलावृष्टि";
        } else if (code >= 80 && code <= 82) {
            data.emoji = "🌦️";
            data.conditionEn = "Heavy Rain Showers";
            data.conditionHi = "तेज बारिश की संभावना";
        } else if (code >= 95) {
            data.emoji = "⛈️";
            data.conditionEn = "Thunderstorm Alert";
            data.conditionHi = "आंधी-तूफान की चेतावनी";
        } else {
            data.emoji = "🌤️";
            data.conditionEn = "Fair Weather";
            data.conditionHi = "अनुकूल मौसम";
        }

        // Generate tailored agricultural advisory
        if (code >= 51 && code <= 99) {
            data.advisoryEn = "⚠️ Rain predicted: Delay crop harvesting, combine cutting & stubble baling. Keep harvested grains & bales under waterproof tarpaulin.";
            data.advisoryHi = "⚠️ बारिश की संभावना: फसल कटाई व पराली बेलिंग रोकें। कटी हुई फसल व पराली को तिरपाल से ढककर सुरक्षित रखें।";
        } else if (wind > 20.0) {
            data.advisoryEn = "💨 High wind warning (" + Math.round(wind) + " km/h): Do NOT spray liquid fertilizers or pesticides today to prevent chemical drift.";
            data.advisoryHi = "💨 तेज हवा की चेतावनी (" + Math.round(wind) + " किमी/घंटा): आज कीटनाशक या खाद का छिड़काव न करें, हवा से नुकसान हो सकता है।";
        } else if (temp >= 36.0) {
            data.advisoryEn = "☀️ High temperature (" + Math.round(temp) + "°C): Irrigate standing crops in evening hours to preserve root moisture. Optimal for stubble sun-drying.";
            data.advisoryHi = "☀️ अधिक तापमान (" + Math.round(temp) + "°C): शाम के समय हल्की सिंचाई करें। पराली व भूसे को धूप में सुखाने के लिए उत्तम समय।";
        } else {
            data.advisoryEn = "✅ Excellent farming conditions: Ideal weather for stubble collection, baler operation, tractor field preparation and mandi transport.";
            data.advisoryHi = "✅ खेती के लिए अनुकूल मौसम: पराली एकत्र करने, बेलर चलाने, जुताई और मंडी परिवहन के लिए सबसे उत्तम समय।";
        }
    }

    private static String cleanCityName(String raw) {
        if (raw == null) return "jaipur";
        String s = raw.toLowerCase()
                .replace("📍", "")
                .replace("•", " ")
                .replace("live agro-radar", "")
                .replace("grain market", "")
                .replace("mandi", "")
                .replace("agro park", "")
                .replace("industrial area", "")
                .replace("hub", "")
                .replace("sez", "")
                .replace(",", " ")
                .replace("mp", "")
                .replace("punjab", "")
                .replace("haryana", "")
                .replace("rajasthan", "")
                .replace("up", "")
                .trim();
        String[] parts = s.split("\\s+");
        return parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "jaipur";
    }

    private static double[] resolveCoordinates(String cityKey) {
        for (Map.Entry<String, double[]> entry : KNOWN_COORDINATES.entrySet()) {
            if (cityKey.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static double[] geocodeCity(String cityName) {
        try {
            String encoded = URLEncoder.encode(cityName, "UTF-8");
            String urlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + encoded + "&count=1&language=en&format=json";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject root = new JSONObject(sb.toString());
                if (root.has("results")) {
                    JSONArray arr = root.getJSONArray("results");
                    if (arr.length() > 0) {
                        JSONObject first = arr.getJSONObject(0);
                        return new double[]{first.getDouble("latitude"), first.getDouble("longitude")};
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Geocoding failed for: " + cityName, e);
        }
        return null;
    }
}
