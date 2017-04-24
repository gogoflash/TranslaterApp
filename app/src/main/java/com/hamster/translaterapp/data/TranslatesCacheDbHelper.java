package com.hamster.translaterapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ПК on 10.04.2017.
 */
public class TranslatesCacheDbHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "translates.db";

    private static final int DATABASE_VERSION = 1;

  /*  public TranslatesCacheDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION, errorHandler);
    }*/

    public TranslatesCacheDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TranslatesCacheTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        TranslatesCacheTable.onUpgrade(db, oldVersion, newVersion);
    }

   /* public void dropTable() {
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS + " + TranslatesCacheTable.TABLE_NAME);
        TranslatesCacheTable.onCreate(getWritableDatabase());
    }*/

    public int deleteCache() {
        return getWritableDatabase().delete(
                TranslatesCacheTable.TABLE_NAME,
                TranslatesCacheTable.COLUMN_IN_HISTORY + " = ? and " + TranslatesCacheTable.COLUMN_IN_FAVORITES + " = ?",
                new String[] {"0", "0"}
        );
    }

    public int deleteTranslate(String column, Boolean delete) {
        return deleteTranslate(column, delete, null);
    }

    public int deleteTranslate(String column, Boolean delete, String hash)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, delete ? 0 : 1);

        String whereClause;
        String[] whereArgs;
        if(hash == null) {
            whereClause = column + " = ?";
            whereArgs = new String[] {delete ? "1" : "0"};
        }
        else {
            whereClause = column + " = ? and " + TranslatesCacheTable.COLUMN_HASH + " = ?";
            whereArgs = new String[] {delete ? "1" : "0", hash};
        }

        return getWritableDatabase().update(
                TranslatesCacheTable.TABLE_NAME,
                contentValues,
                whereClause,
                whereArgs
                );
    }

    public void insertTranslate(TranslateDataItem item)
    {
        ContentValues values = new ContentValues();
        values.put(TranslatesCacheTable.COLUMN_HASH, item.getHash());
        values.put(TranslatesCacheTable.COLUMN_REQUEST_TEXT, item.sourceText);
        values.put(TranslatesCacheTable.COLUMN_RESPONCE_TEXT, item.resultText);
        values.put(TranslatesCacheTable.COLUMN_TRANSLATE_FROM, item.languageFrom);
        values.put(TranslatesCacheTable.COLUMN_TRANSLATE_TO, item.languageTo);
        values.put(TranslatesCacheTable.COLUMN_TIMESTAMP, item.timestamp);
        values.put(TranslatesCacheTable.COLUMN_API, item.api);

        long newRowId = getWritableDatabase().insert(TranslatesCacheTable.TABLE_NAME, null, values);
    }

    public void updateTranslateItem(TranslateDataItem item, Boolean inFavorites, Boolean inHistory)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TranslatesCacheTable.COLUMN_IN_FAVORITES, inFavorites ? 1 : 0);
        contentValues.put(TranslatesCacheTable.COLUMN_IN_HISTORY, inHistory ? 1 : 0);

        getWritableDatabase().update(
                TranslatesCacheTable.TABLE_NAME,
                contentValues,
                TranslatesCacheTable.COLUMN_HASH + " = ?",
                new String[] {item.getHash()});
    }

    public TranslateDataItem findByHash(String hash)
    {
        Cursor cursor = getReadableDatabase().query(
                TranslatesCacheTable.TABLE_NAME,
                null,
                TranslatesCacheTable.COLUMN_HASH + " = ?",
                new String[] {hash},
                null,
                null,
                null
        );

        cursor.moveToFirst();
        if( cursor.getCount() > 0 ) {
            TranslateDataItem item = new TranslateDataItem(cursor);
            cursor.close();
            return  item;
        }
        cursor.close();
       // String item_content = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_RESPONCE_TEXT));

        return null;
    }

    /**
     * TODO  может завершиться неудачно из-за проблем с полномочиями или нехваткой места на диске
     * */
    public void checkWritebleDataBase() {
        try {
            getWritableDatabase();
        }
        catch (SQLiteException ex){
            getReadableDatabase();
        }
    }

}
