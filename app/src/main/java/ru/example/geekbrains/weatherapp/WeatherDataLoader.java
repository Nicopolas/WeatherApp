package ru.example.geekbrains.weatherapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Вспомогательный класс для работы с API openweathermap.org и скачивания нужных
 * данных
 */
class WeatherDataLoader {
    private static final String LOG_TAG = WeatherDataLoader.class.getSimpleName();

    private static final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
    public static final String URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";
    private static final String KEY = "x-api-key";
    private static final String RESPONSE = "cod";
    private static final String NEW_LINE = "\n";
    private static final int ALL_GOOD = 200;

    // Единственный метод класса, который делает запрос на сервер и получает от него данные
    // Возвращает объект JSON или null
    static JSONObject getJSONData(Context context, String city) {
        try {
            // Используем API (Application programming interface) openweathermap
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty(KEY, context.getString(R.string.open_weather_maps_app_id));

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder rawData = new StringBuilder(1024);
            String tempVariable;
            while ((tempVariable = reader.readLine()) != null)
                rawData.append(tempVariable).append(NEW_LINE);
            reader.close();

            JSONObject jsonObject = new JSONObject(rawData.toString());
            // API openweathermap
            if (jsonObject.getInt(RESPONSE) != ALL_GOOD)
                return null;
            return jsonObject;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "error", e); // FIXME Обработка ошибки
            return null;
        }
    }

    static JSONObject getJSONObjectUsingOKHttp(Context context, String city) {
        OkHttpClient client = new OkHttpClient();
        Response response;
        final JSONObject json;

        String url = String.format(URL, city, context.getString(R.string.open_weather_maps_app_id));
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            //выполняем запрос и получаем ответ
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.wtf("OkHttpClient", e.toString());
                return null;
            }

            String jsonData = response.body().string();
            json = new JSONObject(jsonData);

            if (json.getInt(RESPONSE) != ALL_GOOD)
                return null;
            return json;

        } catch (Exception e) {
            e.printStackTrace();
            Log.wtf("response", e.toString());
            return null;
        }
    }
}
