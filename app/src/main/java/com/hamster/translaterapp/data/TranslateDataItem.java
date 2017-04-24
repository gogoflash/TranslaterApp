package com.hamster.translaterapp.data;

import android.database.Cursor;

import com.hamster.translaterapp.utils.MD5;

/**
 * Created by ПК on 10.04.2017.
 */
public class TranslateDataItem {

    public String sourceText;

    public String resultText;

    public long timestamp;

    public String languageFrom;

    public String languageTo;

    // всегда закладывайся на разные апи даже если в начале проекта оно одно и кажется незыблемым :)
    public String api;

    public Boolean inFavorites = false;

    public Boolean inHistory = true;

    private String hash;

    public String getHash() {
        return hash;
    }

    /**
     * Конструктор при заполнении из ответа api
     * */
    public TranslateDataItem(String sourceText, String resultText, long timestamp, String languageFrom, String languageTo, String api) {
        this.sourceText = sourceText;
        this.resultText = resultText;
        this.timestamp = timestamp;
        this.languageFrom = languageFrom;
        this.languageTo = languageTo;
        this.api = api;

        hash = MD5.get(sourceText) + languageTo;
        // тут момент: нельзя формировать хеш, используя languageFrom, так как он неизвестен заранее(а потому не получится вытащить из кеша при том же автоопределении)
    }

    /**
     * Конструктор при выдергивании из базы данных
     * */
    public TranslateDataItem(Cursor cursor) {
        if(cursor == null)
            return;

        sourceText = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_REQUEST_TEXT));
        resultText = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_RESPONCE_TEXT));
        timestamp = cursor.getLong(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TIMESTAMP));
        languageFrom = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_FROM));
        languageTo = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_TO));
        api = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_API));
        hash = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_HASH));
        inFavorites = cursor.getInt(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_IN_FAVORITES)) == 1;
        inHistory = cursor.getInt(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_IN_HISTORY)) == 1;
    }

    public TranslateDataItem() {
    }
}
