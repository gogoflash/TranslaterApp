package com.hamster.translaterapp.fragments;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hamster.translaterapp.R;
import com.hamster.translaterapp.TranslaterModel;

/**
 * Created by ПК on 17.04.2017.
 */
public class SettingsFragment extends Fragment
{
    private Button buttonCleanHistory;
    private Button buttonCleanFavorites;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        buttonCleanHistory = (Button) view.findViewById(R.id.buttonCleanHistory);
        buttonCleanFavorites = (Button) view.findViewById(R.id.buttonCleanFavorites);

        TextView licenseLinkText = (TextView) view.findViewById(R.id.licenseLinkText);
        licenseLinkText.setMovementMethod(LinkMovementMethod.getInstance());
        //licenseLinkText.setText(Html.fromHtml(getResources().getString(R.string.yandex_api_license_link), Html.FROM_HTML_MODE_COMPACT));
        licenseLinkText.setText(Html.fromHtml(getResources().getString(R.string.yandex_api_license_link)));

        buttonCleanHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showCleanInfoToast(TranslaterModel.getIstance().cleanHistory(null));
            }
        });

        buttonCleanFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showCleanInfoToast(TranslaterModel.getIstance().cleanFavorites(null));
            }
        });

        // debug-кнопка
       /* Button buttonCleanCache = (Button) view.findViewById(R.id.buttonCleanCache);
        buttonCleanCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showCleanInfoToast(TranslaterModel.getIstance().cleanCache());
            }
        });*/
    }

    private void showCleanInfoToast(int count) {
        TranslaterModel.getIstance().showInfoToast(getResources().getString(R.string.items_cleaned) + " " + count);
    }
}
