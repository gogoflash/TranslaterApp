package com.hamster.translaterapp.data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.hamster.translaterapp.Main;
import com.hamster.translaterapp.R;
import com.hamster.translaterapp.api.TranslateManager;
import com.hamster.translaterapp.fragments.TranslateFavoritesAdapter;
import com.hamster.translaterapp.fragments.TranslateFragment;
import com.hamster.translaterapp.fragments.TranslateHistoryAdapter;
import com.hamster.translaterapp.utils.MD5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ПК on 11.04.2017.
 *
 * Класс - главная модель и контроллер одновременно. Не отделять их приемлемо на небольшом проекте, дабы не плодить лишние связи.
 */
public class TranslaterModel
{
    public TranslaterModel(Context context)
    {
        this.context = context;

        translateManager = new TranslateManager();

        dataBase = new TranslatesCacheDbHelper(context);

        historyCursorAdapter = new TranslateHistoryAdapter(context, null, 0);
        historyCursorAdapter.swapCursor(getHistoryCursor());

        favoritesCursorAdapter = new TranslateFavoritesAdapter(context, null, 0);

        favoritesCursorAdapter.swapCursor(getFavoritesCursor());

        // получаем список поддерживаемых языков:
        translateManager.requestGetLanguagesList();
    }

    /*****************************************************************************************************
     *
     * Private variables
     *
     *****************************************************************************************************/

    private static TranslaterModel _instance;

    private TranslateManager translateManager;

    private TranslatesCacheDbHelper dataBase;

    private Context context;

    private TranslateFragment translateFragment;

    private CursorAdapter historyCursorAdapter;

    private CursorAdapter favoritesCursorAdapter;

    private TranslateDataItem translateDataItem;

    private ArrayList<String> languagesNamesFrom = new ArrayList<String>();

    private ArrayList<String> languagesCodesFrom = new ArrayList<String>();

    private ArrayList<String> languagesNamesTo = new ArrayList<String>();

    private ArrayList<String> languagesCodesTo = new ArrayList<String>();

    /*****************************************************************************************************
     *
     * Public methods & variables
     *
     *****************************************************************************************************/

    public static void init(Context context)
    {
        if(_instance == null)
            _instance = new TranslaterModel(context);
        else
            _instance.context = context;
    }

    public static TranslaterModel getIstance() {
       return _instance;
    }

    public String translationFromLanguage;

    public String translationToLanguage;

    /***
     * Хоть список направлений перевода языков и одинаков, но в переводе "из" нужно
     * учесть пункт "автоопределение языка", который хардкодится.
     * 4 списка направлений перевода языков(кодов и названий) избыточно.
     * сознательно сэкономил время, не заморачиваясь с красивой реализацией.
     **/
    public ArrayList<String> getLanguagesNamesFrom() {
        return languagesNamesFrom;
    }

    public ArrayList<String> getLanguagesCodesFrom() {
        return languagesCodesFrom;
    }

    public ArrayList<String> getLanguagesNamesTo() {
        return languagesNamesTo;
    }

    public ArrayList<String> getLanguagesCodesTo() {
        return languagesCodesTo;
    }

    public TranslateManager getTranslateManager(){
        return translateManager;
    }

    public void setTranslateFragment(TranslateFragment translateFragment){
        this.translateFragment = translateFragment;
    }

    public CursorAdapter getHistoryCursorAdapter(){
        return historyCursorAdapter;
    }

    public CursorAdapter getFavoritesCursorAdapter(){
        return favoritesCursorAdapter;
    }

    // текущая переведенная фраза
    public TranslateDataItem getTranslateDataItem() {
        return translateDataItem;
    }

    public void dropTranslateDataItem() {
        translateDataItem = null;
    }

    /**
     * Команда на перевод. Либо найдет перевод в кеше и вернет из него, либо запросит внешний сервис.
     *
     * @param text текст для перевода
     * @param languageFromCode код языка с которого переводим. Может быть "auto"
     * @param languageToCode код языка на который переводим
     * */
    public void translate(String text, String languageFromCode, String languageToCode)
    {
        if(text == null || text.equals("") || languageFromCode == null || languageToCode == null)
            return;

        text = text.trim();

        String hash = MD5.get(text) + languageToCode;

        TranslateDataItem item = dataBase.findByHash(hash);

        if(item == null) {
            translateManager.requestTranslate(text, languageFromCode.equals("auto") ? languageToCode : (languageFromCode + "-" + languageToCode));
        }
        else {
            // если перевод был в кеше, то возвращаем его в историю переводов, если он был оттуда удален:
            if(!item.inHistory) {
                dataBase.updateTranslateItem(item, item.inFavorites, true);
                historyCursorAdapter.swapCursor(getHistoryCursor());
                historyCursorAdapter.notifyDataSetChanged();
            }

            setTranslate(item, false, false);
        }
    }

    /**
     * Внесение перевода в базу и обновление необходымых экранов.
     *
     * @param translateDataItem - заполненная модель с переводом
     * @param saveInDataBase - нужно ли сохранять перевод в базу. Не нужно в случае засеччивания
     *                       перевода из истории или избранного.
     * @param goToTranslateFragment - нужно ли перейти в экран перевода
     *
     * */
    public void setTranslate(TranslateDataItem translateDataItem, Boolean saveInDataBase, Boolean goToTranslateFragment)
    {
        this.translateDataItem = translateDataItem;

        if(goToTranslateFragment) {
            if(translationFromLanguage != "auto")
                translationFromLanguage = translateDataItem.languageFrom;
            translationToLanguage = translateDataItem.languageTo;
        }

        if(saveInDataBase) {
            dataBase.insertTranslate(translateDataItem);
            historyCursorAdapter.swapCursor(getHistoryCursor());
            historyCursorAdapter.notifyDataSetChanged();
        }

        if(goToTranslateFragment && context != null)
            ((Main) context).goToFragmnetByIndex(0);

        if(translateFragment != null)
            translateFragment.refreshTranslate(goToTranslateFragment);
    }

    public void putInFavorites(TranslateDataItem item, Boolean value)
    {
        if(item == null)
            return;

        dataBase.updateTranslateItem(item, value, item.inHistory);
        favoritesCursorAdapter.swapCursor(getFavoritesCursor());
        favoritesCursorAdapter.notifyDataSetChanged();
    }

    /**
     * Очистка всей историю или отдельной записи
     *
     * @param itemHash если не null, то удаляет запись с этим хешем
     * */
    public int cleanHistory(String itemHash)
    {
        int rowsDeleted;

        if(itemHash == null) {
            rowsDeleted = dataBase.deleteTranslate(TranslatesCacheTable.COLUMN_IN_HISTORY, true);
            if(translateDataItem != null)
                translateDataItem.inHistory = false;
        }
        else {
            rowsDeleted = dataBase.deleteTranslate(TranslatesCacheTable.COLUMN_IN_HISTORY, true, itemHash);
            if(translateDataItem != null && itemHash.equals(translateDataItem.getHash()))
                translateDataItem.inHistory = false;
        }

        historyCursorAdapter.swapCursor(getHistoryCursor());
        historyCursorAdapter.notifyDataSetChanged();

        return rowsDeleted;
    }

    /**
     * Очистка всего избранного или отдельной записи
     *
     * @param itemHash если не null, то удаляет запись с этим хешем
     * */
    public int cleanFavorites(String itemHash) {
        int rowsDeleted;

        if(itemHash == null) {
            rowsDeleted = dataBase.deleteTranslate(TranslatesCacheTable.COLUMN_IN_FAVORITES, true);
            if(translateDataItem != null)
                translateDataItem.inFavorites = false;
        }
        else {
            rowsDeleted = dataBase.deleteTranslate(TranslatesCacheTable.COLUMN_IN_FAVORITES, true, itemHash);
            if(translateDataItem != null && itemHash.equals(translateDataItem.getHash()))
                translateDataItem.inFavorites = false;
        }

        favoritesCursorAdapter.swapCursor(getFavoritesCursor());
        favoritesCursorAdapter.notifyDataSetChanged();

        if(translateFragment != null)
            translateFragment.refreshFavoritesButton();

        return rowsDeleted;
    }

    public int cleanCache()
    {
        return dataBase.deleteCache();
    }

    /**
     * Установка списка языков. В нашем случае из внешнего сервиса.
     *
     * @param map список языков
     * */
    public void setLanguagesList(Map<String, String> map)
    {
        // Сортируем вверх некоторые самые популярные языки. В полноценном приложении кастомизацию сортировки можно вынести в настройки.

        int i = 0;
        final HashMap<String, Integer> languageCodeSortMap = new HashMap<>();
        languageCodeSortMap.put("ru", i++);
        languageCodeSortMap.put("en", i++);
        languageCodeSortMap.put("de", i++);
        languageCodeSortMap.put("fr", i++);
        languageCodeSortMap.put("it", i++);
        languageCodeSortMap.put("es", i++);
        languageCodeSortMap.put("ja", i++);
        languageCodeSortMap.put("zh", i++);
        languageCodeSortMap.put("ko", i++);

        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String o1, String o2)
            {
                Boolean containsO1 = languageCodeSortMap.containsKey(o1);
                Boolean containsO2 = languageCodeSortMap.containsKey(o2);
                if(containsO1 && containsO2)
                    return languageCodeSortMap.get(o1) - languageCodeSortMap.get(o2);
                else if(containsO1)
                    return -1;
                else if(containsO2)
                    return 1;
                else
                    return -1;
            }
        };

        languagesCodesTo = new ArrayList(map.keySet());
        Collections.sort(languagesCodesTo, comparator);

        languagesCodesFrom = new ArrayList(map.keySet());
        Collections.sort(languagesCodesFrom, comparator);

        languagesNamesTo = new ArrayList<String>();
        languagesNamesFrom = new ArrayList<String>();
        for (String key : languagesCodesTo) {
            languagesNamesTo.add(map.get(key));
            languagesNamesFrom.add(map.get(key));
        }

        if(context != null) {
            languagesCodesFrom.add(0, "auto");
            languagesNamesFrom.add(0, context.getResources().getString(R.string.auto_language_detection));

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            translationFromLanguage = sharedPreferences.getString("TRANSLATE_FROM", "auto");
            translationToLanguage = sharedPreferences.getString("TRANSLATE_TO", "en");
        }

        if(translateFragment != null)
            translateFragment.refreshLanguagesSpinners();

    }

    /**
     * Закрывает клавиатуру. Нужно при перехода из экрана перевода.
     * */
    public void closeKeyBoard(IBinder binder) {
        if(context == null || binder == null)
            return;

        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Показывает алерт окошко с текстом ошибки.
     *
     *  @param title - заголовок окна
     *  @param text - содержание
     * */
    public void showErrorAlert(String title, String text) {
        if(text == null || context == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title == null ? "" : title)
                .setMessage(text)
                .setIcon(null)
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Всплывашка с текстом.
     *
     *  @param text - текст всплывашки
     * */
    public void showInfoToast(String text) {
        if(text == null || context == null)
            return;

        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    private Cursor getHistoryCursor()
    {
        return  dataBase.getReadableDatabase().query(
                TranslatesCacheTable.TABLE_NAME,
                null,
                TranslatesCacheTable.COLUMN_IN_HISTORY + " = 1",
                null,
                null,
                null,
                null
        );
    }

    private Cursor getFavoritesCursor()
    {
        return  dataBase.getReadableDatabase().query(
                TranslatesCacheTable.TABLE_NAME,
                null,
                TranslatesCacheTable.COLUMN_IN_FAVORITES + " = 1",
                null,
                null,
                null,
                null
        );
    }
}
