package ru.example.geekbrains.weatherapp;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONObject;

public class StartedService extends IntentService {

    public static final String INTENT_RESULT = "ru.example.geekbrains.weatherapp.RESULT";
    public static final String ERROR = "error";

    private LocalBroadcastManager localBroadcastManager;

    public StartedService() {
        super("StartedService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final JSONObject json = WeatherDataLoader.getJSONData(
                getApplicationContext(), intent.getStringExtra(Intent.EXTRA_TEXT));
        intent = new Intent(INTENT_RESULT);
        intent.putExtra(Intent.EXTRA_TEXT, json == null ? ERROR: json.toString());
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        return super.onStartCommand(intent, flags, startId);
    }
}
