package ru.example.geekbrains.weatherapp;

import org.json.JSONObject;

public interface ServiceCallbacks {

    void renderWeatherFromInterface(JSONObject json);
}
