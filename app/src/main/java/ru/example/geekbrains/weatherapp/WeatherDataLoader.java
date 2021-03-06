package ru.example.geekbrains.weatherapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
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

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .header(KEY, context.getString(R.string.open_weather_maps_app_id))
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().string());

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
}
