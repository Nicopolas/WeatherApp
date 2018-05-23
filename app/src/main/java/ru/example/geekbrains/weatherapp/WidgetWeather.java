package ru.example.geekbrains.weatherapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetWeather extends AppWidgetProvider {
    public static final String LOG_TAG = AppWidgetProvider.class.getSimpleName();
    private static final String FONT_FILENAME = "fonts/weather.ttf";
    private RemoteViews views;
    private Typeface weatherFont;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                         final int appWidgetId) {
        Intent intent = new Intent(context, StartedService.class);
        intent.putExtra(Intent.EXTRA_TEXT, getCity(context));
        context.startService(intent);

        LocalBroadcastManager localBroadcastManager;
        BroadcastReceiver broadcastReceiver;
        final Handler handler = new Handler();

        localBroadcastManager = LocalBroadcastManager.getInstance(context);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                final String json = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (json.equals(StartedService.ERROR)) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.place_not_found),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(context, json, appWidgetManager, appWidgetId);
                        }
                    });
            }
        };

        localBroadcastManager.registerReceiver(broadcastReceiver,
                new IntentFilter(StartedService.INTENT_RESULT));
    }

    // Обработка загруженных данных и обновление UI
    private void renderWeather(Context context, String json, AppWidgetManager appWidgetManager,
                               int appWidgetId) {
        Log.d(LOG_TAG, "json " + json);
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Model.class, new MainActivity.Deserializer());

            Gson gson = gsonBuilder.create();
            Model model = gson.fromJson(json, Model.class);

            views.setTextViewText(R.id.city_field_widget, model.getCity());
            weatherFont = Typeface.createFromAsset(context.getAssets(), FONT_FILENAME);
            //картинка не отображается
            views.setTextViewText(R.id.weather_icon_widget,
                    getWeatherIcon(context, model.getId(), model.getSunrise() * 1000,model.getSunset() * 1000));
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "One or more fields not found in the JSON data", e); // FIXME Обработка ошибки
        }
    }

    // Найти способ переиспользывать метод из MainActivity
    private String getWeatherIcon(Context context, int actualId, long sunrise, long sunset) {
        int id = actualId / 100; // Упрощение кодов (int оставляет только целочисленное значение)
        String icon = "";

        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset)
                icon = context.getString(R.string.weather_sunny);
            else
                icon = context.getString(R.string.weather_clear_night);
        }
        else {
            Log.d(LOG_TAG, "id " + id);
            switch (id) {
                case 2:
                    icon = context.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = context.getString(R.string.weather_drizzle);
                    break;
                case 5:
                    icon = context.getString(R.string.weather_rainy);
                    break;
                case 6:
                    icon = context.getString(R.string.weather_snowy);
                    break;
                case 7:
                    icon = context.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = context.getString(R.string.weather_cloudy);
                    break;

                // Можете доработать приложение, найдя все иконки и распарсив все значения
                default:
                    break;
            }
        }
       return icon;
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    private String getCity(Context context) {
            SharedPreferences sPref = context.getSharedPreferences(context.getString(R.string.preferences_city), MODE_PRIVATE);
            return sPref.getString(context.getString(R.string.preferences_city), "");
    }
}

