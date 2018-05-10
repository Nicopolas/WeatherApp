package ru.example.geekbrains.weatherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 10.05.2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weather.db"; // название бд
    private static final int DATABASE_VERSION = 1; // версия базы данных

    public static final String TABLE_NOTES = "notes"; // название таблицы в бд

    // названия столбцов
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_SUNRISE = "sunrise";
    public static final String COLUMN_SUNSET = "sunset";
    public static final String COLUMN_TEMPERATURE = "temperature";
    public static final String COLUMN_HUMIDITY = "humidity";
    public static final String COLUMN_PRESSURE = "pressure";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TIME = "time";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NOTES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CITY + " TEXT,"
                + COLUMN_COUNTRY + " TEXT," + COLUMN_TIME + " INTEGER,"
                + COLUMN_SUNRISE + " INTEGER," + COLUMN_SUNSET + " INTEGER,"
                + COLUMN_TEMPERATURE + " REAL," + COLUMN_HUMIDITY + " TEXT,"
                + COLUMN_PRESSURE + " TEXT," + COLUMN_DESCRIPTION + " TEXT" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if ((oldVersion == 1) && (newVersion == 2)) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
            onCreate(sqLiteDatabase);
        }
    }
}
