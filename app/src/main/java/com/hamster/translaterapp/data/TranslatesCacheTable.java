package com.hamster.translaterapp.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ПК on 10.04.2017.
 */
public class TranslatesCacheTable {
    public static final String TABLE_NAME = "articles";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HASH = "item_hash";
    public static final String COLUMN_REQUEST_TEXT = "text_in";
    public static final String COLUMN_RESPONCE_TEXT = "text_out";
    public static final String COLUMN_IN_FAVORITES = "in_favorites";
    public static final String COLUMN_IN_HISTORY = "in_history";
    public static final String COLUMN_TRANSLATE_FROM = "from_language";
    public static final String COLUMN_TRANSLATE_TO = "to_language";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_API = "api";

    public static void onCreate(SQLiteDatabase sqLiteDatabase)
    {
       String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_HASH + " TEXT NOT NULL, "
                + COLUMN_REQUEST_TEXT + " TEXT NOT NULL, "
                + COLUMN_RESPONCE_TEXT + " TEXT NOT NULL, "
                + COLUMN_IN_FAVORITES + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_IN_HISTORY + " INTEGER NOT NULL DEFAULT 1, "
                + COLUMN_TRANSLATE_FROM + " TEXT NOT NULL, "
                + COLUMN_TRANSLATE_TO + " TEXT NOT NULL, "
                + COLUMN_TIMESTAMP + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_API + " STRING NOT NULL "
                + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
