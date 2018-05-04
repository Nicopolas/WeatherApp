package ru.example.geekbrains.weatherapp;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by user on 04.05.2018.
 */

public class WeatherObject {

    String name;
    String country;
    ArrayList weather;
    String description;
    JSONObject main;
    JSONObject details;
    JSONObject sys;
    String humidity;
    String pressure;
    String temp;
    long dt;
    int id;
    long sunrise;

    public long getSunrise() throws JSONException {
        return sys.getLong("sunrise");
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() throws JSONException {
        return sys.getLong("sunset");
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    long sunset;

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public int getId() throws JSONException {
        return details.getInt("id");
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getDescription() throws JSONException {
        return details.getString("description");
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() throws JSONException {
        return sys.getString("country");
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public ArrayList getWeather() {
        return weather;
    }

    public void setWeather(ArrayList weather) {
        this.weather = weather;
    }
}
