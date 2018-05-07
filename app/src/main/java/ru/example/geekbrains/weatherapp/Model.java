package ru.example.geekbrains.weatherapp;

import com.google.gson.annotations.SerializedName;

public class Model {

    @SerializedName("name")
    private String city;

    private String country;

    @SerializedName("dt")
    private Long time;

    private Long sunrise, sunset;
    private Double temperature;
    private String humidity, pressure, description;
    private Integer id;

    public Model(String city, Long time) {
        this.city = city;
        this.time = time;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String value) {
        country = value;
    }

    public Long getTime() {
        return time;
    }

    public Long getSunrise() {
        return sunrise;
    }

    public void setSunrise(Long value) {
        sunrise = value;
    }

    public Long getSunset() {
        return sunset;
    }
    public void setSunset(Long value) {
        sunset = value;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double value) {
        temperature = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer value) {
        id = value;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String value) {
        humidity = value;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String value) {
        pressure = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }
}
