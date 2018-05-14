package ru.example.geekbrains.weatherapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Классовые переменные
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FONT_FILENAME = "fonts/weather.ttf";
    private static final String POSITIVE_BUTTON_TEXT = "Go";
    public Model model;

    // Handler - это класс, позволяющий отправлять и обрабатывать сообщения и объекты runnable.
    // Он используется в двух случаях - когда нужно применить объект runnable когда-то в будущем,
    // и когда необходимо передать другому потоку
    // выполнение какого-то метода. Второй случай наш.
    private final Handler handler = new Handler();

    //Реализация иконок погоды через шрифт (но можно и через картинки)
    private Typeface weatherFont;
    private TextView cityTextView;
    private TextView updatedTextView;
    private TextView detailsTextView;
    private TextView currentTemperatureTextView;
    private TextView weatherIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityTextView = findViewById(R.id.city_field);
        updatedTextView = findViewById(R.id.updated_field);
        detailsTextView = findViewById(R.id.details_field);
        currentTemperatureTextView = findViewById(R.id.current_temperature_field);
        weatherIcon = findViewById(R.id.weather_icon);

        weatherFont = Typeface.createFromAsset(getAssets(), FONT_FILENAME);
        weatherIcon.setTypeface(weatherFont);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("WrongViewCast") FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendOut();
                }
            });
        } else {
            ImageButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendOut();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_city) {
            showInputDialog();
            return true;
        }
        return false;
    }

    // Показываем диалоговое окно с выбором города
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_city_dialog));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(POSITIVE_BUTTON_TEXT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    // Обновление/загрузка погодных данных
    private void updateWeatherData(final String city) {
        new Thread() { //Отдельный поток для получения новых данных в фоне
            public void run() {
                final JSONObject json = WeatherDataLoader.getJSONData(getApplicationContext(), city);
                // Вызов методов напрямую может вызвать runtime error
                // Мы не можем напрямую обновить UI, поэтому используем handler,
                // чтобы обновить интерфейс в главном потоке.
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.place_not_found),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(json);
                        }
                    });
            }
        }.start();
    }

    public static class Deserializer implements JsonDeserializer<Model> {
        @Override
        public Model deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonRoot = json.getAsJsonObject();

            Model model = new Model(jsonRoot.get("name").getAsString(),
                    jsonRoot.get("dt").getAsLong());

            JsonObject jsonSys = jsonRoot.get("sys").getAsJsonObject();

            model.setCountry(jsonSys.get("country").getAsString());
            model.setSunrise(jsonSys.get("sunrise").getAsLong());
            model.setSunset(jsonSys.get("sunset").getAsLong());

            JsonObject jsonMain = jsonRoot.get("main").getAsJsonObject();
            model.setTemperature(jsonMain.get("temp").getAsDouble());
            model.setHumidity(jsonMain.get("humidity").getAsString());
            model.setPressure(jsonMain.get("pressure").getAsString());

            JsonObject jsonWeather = jsonRoot.getAsJsonArray("weather")
                    .get(0).getAsJsonObject();
            model.setId(jsonWeather.get("id").getAsInt());
            model.setDescription(jsonWeather.get("description").getAsString());

            return model;
        }
    }

    // Обработка загруженных данных и обновление UI
    private void renderWeather(JSONObject json) {
        Log.d(LOG_TAG, "json " + json.toString());
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Model.class, new Deserializer());

            Gson gson = gsonBuilder.create();
            model = gson.fromJson(json.toString(), Model.class);

            saveToDb(model);

            Resources res = getResources();

            cityTextView.setText(String.format("%s, %s",
                    model.getCity().toUpperCase(Locale.US), model.getCountry()));

            String updatedOn = DateFormat.getDateTimeInstance().format(
                    new Date(model.getTime() * 1000));
            updatedTextView.setText(res.getString(R.string.last_update, updatedOn));

            currentTemperatureTextView.setText(
                    String.format(Locale.US, "%.2f ℃", model.getTemperature()));

            detailsTextView.setText(String.format("%s\nHumidity: %s%%\nPressure: %s hPa",
                    model.getDescription().toUpperCase(Locale.US),
                    model.getHumidity(), model.getPressure()));

            setWeatherIcon(model.getId(), model.getSunrise() * 1000,
                    model.getSunset() * 1000);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "One or more fields not found in the JSON data", e); // FIXME Обработка ошибки
        }
    }

    // Подстановка нужной иконки
    // Парсим коды http://openweathermap.org/weather-conditions
    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100; // Упрощение кодов (int оставляет только целочисленное значение)
        String icon = "";

        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset)
                icon = getString(R.string.weather_sunny);
            else
                icon = getString(R.string.weather_clear_night);
        }
        else {
            Log.d(LOG_TAG, "id " + id);
            switch (id) {
                case 2:
                    icon = getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getString(R.string.weather_drizzle);
                    break;
                case 5:
                    icon = getString(R.string.weather_rainy);
                    break;
                case 6:
                    icon = getString(R.string.weather_snowy);
                    break;
                case 7:
                    icon = getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = getString(R.string.weather_cloudy);
                    break;

                // Можете доработать приложение, найдя все иконки и распарсив все значения
                default:
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    // Метод для доступа кнопки меню к данным
    public void changeCity(String city) {
        updateWeatherData(city);
    }

    private Uri getUri() {
        return Uri.parse(String.format("%s/%s",
                String.format(CacheProvider.BASE_URI, getApplication().getPackageName()),
                CacheProvider.TABLE_NAME));
    }

    private void saveToDb(Model model) {
        ContentValues values = new ContentValues();

        values.put(CacheProvider.COLUMN_CITY, model.getCity());
        values.put(CacheProvider.COLUMN_COUNTRY, model.getCountry());
        values.put(CacheProvider.COLUMN_SUNRISE, model.getSunrise());
        values.put(CacheProvider.COLUMN_SUNSET, model.getSunset());
        values.put(CacheProvider.COLUMN_TEMPERATURE, model.getTemperature());
        values.put(CacheProvider.COLUMN_HUMIDITY, model.getHumidity());
        values.put(CacheProvider.COLUMN_PRESSURE, model.getPressure());
        values.put(CacheProvider.COLUMN_DESCRIPTION, model.getDescription());
        values.put(CacheProvider.COLUMN_TIME, model.getTime());

        ContentResolver contentResolver = getContentResolver();

        String[] projection = new String[] { BaseColumns._ID };
        String selection = CacheProvider.COLUMN_CITY + "=?";
        String[] selectionArgs = new String[] { model.getCity() };

        Cursor cursor = contentResolver.query(getUri(), projection, selection, selectionArgs, null);
        try {
            if (cursor.getCount() > 0)
                contentResolver.update(getUri(), values, selection, selectionArgs);
            else
                contentResolver.insert(getUri(), values);
        }
        finally {
            cursor.close();
        }
    }


    public void sendOut() {
        if (model == null){
            makeToast(getString(R.string.dont_found_weather_data));
            return;
        }
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        String massage = String.format("%s.%s %s ℃", model.getCity(), model.getCountry(), model.getTemperature());
        i.putExtra(Intent.EXTRA_TEXT, massage);
        startActivity(i);
    }//Поделиться

    public void makeToast(String string) {
        Toast toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
        toast.show();
    }
}
