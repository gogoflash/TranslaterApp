package com.hamster.translaterapp.fragments;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hamster.translaterapp.R;
import com.hamster.translaterapp.data.TranslatesCacheTable;

/**
 * Created by ПК on 18.04.2017.
 */
public class TranslateFavoritesAdapter  extends CursorAdapter {
    public TranslateFavoritesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.favorite_item_view, parent, false);
        FavoritesItemViewHolder holder = new FavoritesItemViewHolder();

        holder.sourceTextView = (TextView) view.findViewById(R.id.favoriteItemSourceText);
        holder.resultTextView = (TextView) view.findViewById(R.id.favoriteItemResultText);
        holder.langFromTextView = (TextView) view.findViewById(R.id.favoriteItemLangFromText);
        holder.langToTextView = (TextView) view.findViewById(R.id.favoriteItemLangToText);

        populateView(holder, cursor, context);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        FavoritesItemViewHolder holder = (FavoritesItemViewHolder) view.getTag();
        populateView(holder, cursor, context);
    }

    private void populateView(FavoritesItemViewHolder holder, Cursor cursor, Context context) {
        holder.sourceTextView.setText(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_REQUEST_TEXT)));
        holder.resultTextView.setText(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_RESPONCE_TEXT)));
        //String lang = cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_FROM)) + "\n" + cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_TO));
        holder.langFromTextView.setText(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_FROM)));
        holder.langToTextView.setText(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_TRANSLATE_TO)));
    }

    class FavoritesItemViewHolder {
        public TextView sourceTextView;
        public TextView resultTextView;
        public TextView langFromTextView;
        public TextView langToTextView;
    }
}
