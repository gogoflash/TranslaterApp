package com.hamster.translaterapp.fragments;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hamster.translaterapp.R;
import com.hamster.translaterapp.data.TranslatesCacheTable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ПК on 14.04.2017.
 */
public class TranslateHistoryAdapter extends CursorAdapter
{
    public TranslateHistoryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.history_view_item, parent, false);
        HistoryItemViewHolder holder = new HistoryItemViewHolder();

        holder.sourceTextView = (TextView) view.findViewById(R.id.historyItemSourceText);
        holder.resultTextView = (TextView) view.findViewById(R.id.historyItemResultText);
        holder.langFromTextView = (TextView) view.findViewById(R.id.historyItemLangFromText);
        holder.langToTextView = (TextView) view.findViewById(R.id.historyItemLangToText);

        populateView(holder, cursor, context);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        HistoryItemViewHolder holder = (HistoryItemViewHolder) view.getTag();
        populateView(holder, cursor, context);
    }

    private void populateView(HistoryItemViewHolder holder, Cursor cursor, Context context) {
        holder.sourceTextView.setText(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_REQUEST_TEXT)));
        holder.resultTextView.setText(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_RESPONCE_TEXT)));
        //String lang = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_FROM)) + "\n" + cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_TO));
        holder.langFromTextView.setText(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_FROM)));
        holder.langToTextView.setText(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_TO)));


        /* заготовка для светлого будушего:
        Date date = new java.util.Date();
        date.setTime(cursor.getLong(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TIMESTAMP)));
        String s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        Log.d("translaterApp", s);*/
    }

    class HistoryItemViewHolder {
        public TextView sourceTextView;
        public TextView resultTextView;
        public TextView langFromTextView;
        public TextView langToTextView;
    }
}
