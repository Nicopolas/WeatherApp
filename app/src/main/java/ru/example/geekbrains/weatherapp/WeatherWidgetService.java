package ru.example.geekbrains.weatherapp;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WeatherWidgetService extends RemoteViewsService {
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetWeatherFactory(this.getApplicationContext(), intent);
    }
}