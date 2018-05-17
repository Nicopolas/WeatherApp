package ru.example.geekbrains.weatherapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateWeatherDataService extends Service {
    private static final String LOG_TAG = UpdateWeatherDataService.class.getSimpleName();
    private final Handler handler = new Handler();
    private Timer timer;
    private TimerTask tTask;
    private long time = 0;

    private final IBinder mBinder = new LocalBinder();
    private static ServiceCallbacks serviceCallbacks;

    public UpdateWeatherDataService() {
    }

    @Override
    public void onCreate() {
        timer = new Timer();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, getString(R.string.bind_service_massage),
                Toast.LENGTH_SHORT).show();
        final String city = intent.getStringExtra(MainActivity.CITY_EXTRA);

        if (tTask != null) tTask.cancel();
        tTask = new TimerTask() {
            public void run() {
                Log.d("Service", " " + time);
                updateWeatherData(city);
                time++;
            }
        };
        timer.schedule(tTask, 1000, 300000);
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, getString(R.string.unbind_service_massage),
                Toast.LENGTH_SHORT).show();

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

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
                            Log.d("Service", getString(R.string.place_not_found));
                            Toast.makeText(getApplicationContext(), getString(R.string.place_not_found),
                                    Toast.LENGTH_SHORT).show();

                            serviceCallbacks.renderWeatherFromInterface(json);
                        }
                    });
                } else
                    handler.post(new Runnable() {
                        public void run() {
                            serviceCallbacks.renderWeatherFromInterface(json);
                        }
                    });
            }
        }.start();
    }

    public class LocalBinder extends Binder {
        UpdateWeatherDataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return UpdateWeatherDataService.this;
        }
    }

    public static void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }
}
