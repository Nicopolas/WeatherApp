package ru.example.geekbrains.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Классовые переменные
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FONT_FILENAME = "fonts/weather.ttf";
    private static final String POSITIVE_BUTTON_TEXT = "Go";

    /*  public FragmentManager fm = getSupportFragmentManager();
        public DialogFragment dialogFragment = fm.findFragmentById(R.id.fragment_container);*/
    public DialogFragment dialogFragment;

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

    public  WeatherObject weather;

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
                final JSONObject json = WeatherDataLoader.getJSONObjectUsingOKHttp(getApplicationContext(), city);

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
                            renderWeatherFronObject(json);
                            parseJsonInWeatherObject(json);
                        }
                    });
            }
        }.start();
    }
    private void parseJsonInWeatherObject(JSONObject json){
        //попытка создать обьект из JSONObject
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        weather = gson.fromJson(json.toString(), WeatherObject.class);
    }

    // Обработка загруженных данных и обновление UI (без желтого)
    private void renderWeather(JSONObject json) {
        Log.d(LOG_TAG, "json " + json.toString());
        try {
            cityTextView.setText(String.format("%S, %S",
                    json.getString("name").toUpperCase(Locale.US),
                    json.getJSONObject("sys").getString("country")));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");

            detailsTextView.setText(String.format("%S%nHumidity: %S%%%nPressure: %S hPa",
                    details.getString("description").toUpperCase(Locale.US),
                    main.getString("humidity"), main.getString("pressure")));

            String currentTemperatureText = String.format("%S ℃", new DecimalFormat("##0.00").format(main.getDouble("temp")));
            currentTemperatureTextView.setText(currentTemperatureText);

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));

            updatedTextView.setText(String.format("Last update: %S", updatedOn));

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        } catch (Exception e) {
            Log.e(LOG_TAG, "One or more fields not found in the JSON data", e); // FIXME Обработка ошибки
        }
    }

    //попытка обновление UI из WeatherObject
    private void renderWeatherFronObject(JSONObject json) {
        Log.d(LOG_TAG, "json " + json.toString());
        try {
            cityTextView.setText(String.format("%S, %S",
                    weather.getName().toUpperCase(Locale.US),
                    weather.getCountry()));

            detailsTextView.setText(String.format("%S%nHumidity: %S%%%nPressure: %S hPa",
                    weather.getDescription().toUpperCase(Locale.US),
                    weather.getHumidity(), weather.getPressure()));

            String currentTemperatureText = String.format("%S ℃", new DecimalFormat("##0.00").format(weather.getTemp()));
            currentTemperatureTextView.setText(currentTemperatureText);

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(weather.getDt() * 1000));

            updatedTextView.setText(String.format("Last update: %S", updatedOn));

            setWeatherIcon(weather.getId(),
                    weather.getSunrise() * 1000,
                    weather.getSunset() * 1000);

        } catch (Exception e) {
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
        } else {
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
}
