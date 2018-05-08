package ru.example.geekbrains.weatherapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FONT_FILENAME = "fonts/weather.ttf";
    private static final String POSITIVE_BUTTON_TEXT = "Go";

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
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_drawer_layout);

        cityTextView = findViewById(R.id.city_field);
        updatedTextView = findViewById(R.id.updated_field);
        detailsTextView = findViewById(R.id.details_field);
        currentTemperatureTextView = findViewById(R.id.current_temperature_field);
        weatherIcon = findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(getAssets(), FONT_FILENAME);
        weatherIcon.setTypeface(weatherFont);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        imageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.avatarImageView);

        String oldCity = loadFromPreferences(R.string.save_city_prefs_key);
        if (!oldCity.isEmpty()) {
            changeCity(oldCity);
        }

        try {
            saveAvatarInInternalStorage(this);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        loadAvatarFromInternalStorage(this);


        try {
            saveAvatarInExternalStorage(this);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        loadAvatarFromExternalStorage(this);
    }

    @Override
    public void onBackPressed() {
        //обработка нажатия в Navigation Drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
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

    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_main_windows:
                makeToast("В разарботке");
                break;
            case R.id.nav_manage:
                makeToast("В разарботке");
                break;
            case R.id.nav_about_developers:
                makeToast("В разарботке");
                break;
            case R.id.nav_share:
                sendOut();
            case R.id.nav_exit_app:
                finish();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // закрываем NavigationView, параметр определяет анимацию закрытия
        drawer.closeDrawer(GravityCompat.START);

        return true;
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
            Model model = gson.fromJson(json.toString(), Model.class);

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
        saveToPreferences(R.string.save_city_prefs_key, city);
    }

    //метод выгрузки данных из Preferences
    private String loadFromPreferences(int key) {
        SharedPreferences sharedPref = getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);

        return sharedPref.getString(getString(key), "");
    }

    //метод для сохранения данных в Preferences
    private void saveToPreferences(int key, String value) {
        SharedPreferences sharedPref = getSharedPreferences(LOG_TAG,
                Context.MODE_PRIVATE);
        if (value != null && !value.toString().isEmpty())
            sharedPref.edit().putString(getString(key),
                    value.toString()).apply();
    }

    private void makeToast(String string) {
        Toast toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void sendOut() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "text");
        startActivity(i);
    }//Поделиться

    //запись аватара в InternalStorage
    private void saveAvatarInInternalStorage(Context context) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.sym_def_app_icon, options);

        //FileOutputStream outputStream = openFileOutput(context.getFilesDir() + getString(R.string.avatar_file_name), Context.MODE_PRIVATE);
        FileOutputStream outputStream = new FileOutputStream(context.getFilesDir() + getString(R.string.avatar_file_name));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    private void loadAvatarFromInternalStorage(Context context) {
        Bitmap bitmap = BitmapFactory.decodeFile(context.getFilesDir().toString() + getString(R.string.avatar_file_name));
        imageView.setImageBitmap(bitmap);
    }

    private void saveAvatarInExternalStorage(Context context) throws IOException {
        File file = new
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS
        ), getString(R.string.avatar_file_name));

        if (!isExternalStorageWritable()) {
            makeToast(getString(R.string.toast_external_storage_not_found));
            return;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(file, false);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.sym_def_app_icon, options);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            makeToast(e.getMessage());
        }

    }

    private void loadAvatarFromExternalStorage(Context context) {
        File file = new
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                , getString(R.string.avatar_file_name));
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        imageView.setImageBitmap(bitmap);

    }

    private boolean isExternalStorageWritable() {
        return true;
    }
}
