package ru.example.geekbrains.weatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 10.05.2018.
 */

public class WeatherDataSource {
    public static final String TAG = "WeatherDataSource";
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private final String[] modelAllColumn = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_CITY,
            DatabaseHelper.COLUMN_COUNTRY,
            DatabaseHelper.COLUMN_SUNRISE,
            DatabaseHelper.COLUMN_SUNSET,
            DatabaseHelper.COLUMN_TEMPERATURE,
            DatabaseHelper.COLUMN_HUMIDITY,
            DatabaseHelper.COLUMN_PRESSURE,
            DatabaseHelper.COLUMN_DESCRIPTION,
            DatabaseHelper.COLUMN_TIME
    };

    public WeatherDataSource(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addCity(Model model) {
        if (checkExistingCityOnDataBase(model.getCity())) {
            editCity(model);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CITY, model.getCity());
        values.put(DatabaseHelper.COLUMN_COUNTRY, model.getCountry());
        values.put(DatabaseHelper.COLUMN_SUNRISE, model.getSunrise());
        values.put(DatabaseHelper.COLUMN_SUNSET, model.getSunset());
        values.put(DatabaseHelper.COLUMN_TEMPERATURE, model.getTemperature());
        values.put(DatabaseHelper.COLUMN_HUMIDITY, model.getHumidity());
        values.put(DatabaseHelper.COLUMN_PRESSURE, model.getPressure());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, model.getDescription());
        values.put(DatabaseHelper.COLUMN_TIME, model.getTime());

        long insertId = database.insert(DatabaseHelper.TABLE_NOTES, null,
                values);
    }

    public void editCity(Model model) {
        if (!checkExistingCityOnDataBase(model.getCity())) {
            Log.wtf(TAG,"City not exist in database");
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CITY, model.getCity());
        values.put(DatabaseHelper.COLUMN_COUNTRY, model.getCountry());
        values.put(DatabaseHelper.COLUMN_SUNRISE, model.getSunrise());
        values.put(DatabaseHelper.COLUMN_SUNSET, model.getSunset());
        values.put(DatabaseHelper.COLUMN_TEMPERATURE, model.getTemperature());
        values.put(DatabaseHelper.COLUMN_HUMIDITY, model.getHumidity());
        values.put(DatabaseHelper.COLUMN_PRESSURE, model.getPressure());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, model.getDescription());
        values.put(DatabaseHelper.COLUMN_TIME, model.getTime());

        database.update(DatabaseHelper.TABLE_NOTES, values,
                DatabaseHelper.COLUMN_ID + "=" + model.getId(), null);
    }

    public int getIdCity(String city) {
        if (checkExistingCityOnDataBase(city)) {
            Log.wtf(TAG,"City not exist in database");
        }
        Cursor cursor = database.query(DatabaseHelper.TABLE_NOTES,
                modelAllColumn, "city LIKE '" + city + "'", null,
                null, null, null);
        cursor.moveToFirst();
        return cursorToModel(cursor).getId();
    }

    public boolean checkExistingCityOnDataBase(String city) {
        return database.query(DatabaseHelper.TABLE_NOTES,
                modelAllColumn, "city LIKE '" + city + "'", null,
                null, null, null).moveToFirst();
    }

    private Model cursorToModel(Cursor cursor) {
        //проверить захардкоженные числа
        Model model = new Model(cursor.getString(1),cursor.getLong(3));
        model.setId(cursor.getInt(0));
        model.setCountry(cursor.getString(2));
        model.setSunrise(cursor.getLong(4));
        model.setSunset(cursor.getLong(5));
        model.setTemperature(cursor.getDouble(6));
        model.setHumidity(cursor.getString(7));
        model.setPressure(cursor.getString(8));
        model.setDescription(cursor.getString(9));
        return model;
    }
}
