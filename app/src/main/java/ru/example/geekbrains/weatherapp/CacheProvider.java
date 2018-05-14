package ru.example.geekbrains.weatherapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class CacheProvider extends ContentProvider {

    public static final int             DATABASE_VERSION    = 1;
    public static final String          DATABASE_NAME       = "weather.db";

    public static final String          TABLE_NAME          = "data";
    public static final String          COLUMN_CITY         = "city";
    public static final String          COLUMN_COUNTRY      = "country";
    public static final String          COLUMN_SUNRISE      = "sunrise";
    public static final String          COLUMN_SUNSET       = "sunset";
    public static final String          COLUMN_TEMPERATURE  = "temperature";
    public static final String          COLUMN_HUMIDITY     = "humidity";
    public static final String          COLUMN_PRESSURE     = "pressure";
    public static final String          COLUMN_DESCRIPTION  = "description";
    public static final String          COLUMN_TIME         = "time";

    public static final String          BASE_URI            = "content://%s.provider";

    private static final String         MIME_DIR            = "vnd.android.cursor.dir";
    private static final String         MIME_ITEM           = "vnd.android.cursor.item";
    private static final String         MIME_SUBTYPE        = "/vnd.%s.%s";

    public  static final String         SELECTION_ID        = BaseColumns._ID + "=?";

    private static final String[]       MIN_COLUMNS         = new String[] { BaseColumns._ID };
    private static final Cursor         EMPTY_CURSOR        = new MatrixCursor(MIN_COLUMNS, 0);

    private final Matcher mUriMatcher = new Matcher();
    private WeatherDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        String table = String.format(MIME_SUBTYPE, uri.getAuthority(), TABLE_NAME);

        switch (mUriMatcher.match(uri)) {
            case ALL:
                return MIME_DIR + table;
            case ID:
                return MIME_ITEM + table;
            default:
                logError("unknown uri " + uri);
                return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String tableName = TABLE_NAME;

        switch (mUriMatcher.match(uri)) {       // fall through
            case ID:
                selection       = SELECTION_ID;
                selectionArgs   = getSelectionIdArgs(uri);

            case ALL:
                // from docs: To remove all rows and get a count pass "1" as the whereClause.
                if (selection == null) selection = "1";
                return mDbHelper.getWritableDatabase().delete(
                        tableName, selection, selectionArgs);

            default:
                logError("unknown uri " + uri);
                return 0;
        }
    }

    private static String[] getSelectionIdArgs(@NonNull Uri uri) {
        return new String[] { uri.getLastPathSegment() };
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String tableName = TABLE_NAME;

        return ContentUris.withAppendedId(uri,
                mDbHelper.getWritableDatabase().insert(tableName, null, values));
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName = TABLE_NAME;

        switch (mUriMatcher.match(uri)) {       // fall through
            case ID:
                selection       = SELECTION_ID;
                selectionArgs   = getSelectionIdArgs(uri);

            case ALL:
                return mDbHelper.getWritableDatabase().update(
                        tableName, values, selection, selectionArgs);

            default:
                logError("unknown uri " + uri);
                return 0;
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName = TABLE_NAME;

        switch (mUriMatcher.match(uri)) {       // fall through
            case ID:
                selection       = SELECTION_ID;
                selectionArgs   = getSelectionIdArgs(uri);

            case ALL:
                try {
                    return mDbHelper.getReadableDatabase().query(
                            tableName, projection, selection, selectionArgs, null, null, sortOrder);
                }
                catch (SQLException e) {
                    logError("table " + tableName + ", " + e);
                    return EMPTY_CURSOR;
                }

            default:
                logError("unknown uri " + uri);
                return EMPTY_CURSOR;
        }
    }

    private void logError(String str) {
        Log.e("CacheProvider", str);
    }

    private static class WeatherDbHelper extends SQLiteOpenHelper {

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT, " +
                        COLUMN_TIME + " INTEGER, " + COLUMN_SUNRISE + " INTEGER, " +
                        COLUMN_SUNSET + " INTEGER, " + COLUMN_TEMPERATURE + " REAL, " +
                        COLUMN_HUMIDITY + " TEXT, " + COLUMN_PRESSURE + " TEXT, " +
                        COLUMN_DESCRIPTION + " TEXT);";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public WeatherDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static class Matcher {

        private enum Match {
            NO,
            ALL,
            ID
        }

        @NonNull
        private Match match(@NonNull final Uri uri) {
            List<String> pathSegments = uri.getPathSegments();
            int pathSegmentsSize = pathSegments.size();

            if (pathSegmentsSize == 1)
                return Match.ALL;
            else if (pathSegmentsSize == 2 && TextUtils.isDigitsOnly(pathSegments.get(1)))
                return Match.ID;
            else
                return Match.NO;
        }
    }
}
