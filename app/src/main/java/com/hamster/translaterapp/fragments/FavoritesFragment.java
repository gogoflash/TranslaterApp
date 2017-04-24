package com.hamster.translaterapp.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hamster.translaterapp.R;
import com.hamster.translaterapp.TranslaterModel;
import com.hamster.translaterapp.data.TranslateDataItem;
import com.hamster.translaterapp.data.TranslatesCacheTable;

import java.util.ArrayList;

/**
 * Created by ПК on 10.04.2017.
 */
public class FavoritesFragment extends Fragment implements AdapterView.OnItemClickListener {

    final static int CONTEXT_MENU_ID_DELETE = 11;

    private ListView listView;

    public FavoritesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorites, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(R.id.favoritesListView);
        listView.setAdapter(TranslaterModel.getIstance().getFavoritesCursorAdapter());
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

        //cursor.moveToFirst();
        if( cursor.getCount() > 0 ) {
            TranslateDataItem translateDataItem = new TranslateDataItem(cursor);
            TranslaterModel.getIstance().setTranslate(translateDataItem, false, true);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v == listView)
            menu.add(Menu.NONE, FavoritesFragment.CONTEXT_MENU_ID_DELETE, Menu.NONE, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getItemId() == CONTEXT_MENU_ID_DELETE)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Cursor cursor = (Cursor) listView.getAdapter().getItem(info.position);
            if( cursor != null && cursor.getCount() > 0 ) {
                TranslaterModel.getIstance().cleanFavorites(cursor.getString(cursor.getColumnIndex(TranslatesCacheTable.COLUMN_HASH)));
            }

            return true;
        }

        return super.onContextItemSelected(item);
    }
}
