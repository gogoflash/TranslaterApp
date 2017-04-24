package com.hamster.translaterapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hamster.translaterapp.R;
import com.hamster.translaterapp.data.TranslaterModel;
import com.hamster.translaterapp.data.TranslateDataItem;


/**
 * Created by ПК on 10.04.2017.
 */
public class TranslateFragment extends Fragment implements TextView.OnEditorActionListener, TextWatcher, AdapterView.OnItemSelectedListener
{
    private EditText textInput;
    private TextView textOutput;
    private ImageButton buttonTranslate;
    private ToggleButton buttonFavorites;
    private ImageButton buttonClear;
    private Spinner spinnerTranslateTo;
    private Spinner spinnerTranslateFrom;

    private ArrayAdapter<String> translateToSpinnerAdapter;
    private ArrayAdapter<String> translateFromSpinnerAdapter;

    private TranslateDataItem currentTranslateDataItem;

    private Handler translateCheckHandler;
    private String textInTranslate;

    private int lastSpinnerTranslateToPosition;
    private int lastSpinnerTranslateFromPosition;

    public TranslateFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TranslaterModel.getIstance().setTranslateFragment(this);
        return inflater.inflate(R.layout.translate, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if(translateCheckHandler == null)
            translateCheckHandler = new Handler();

        textInput = (EditText) view.findViewById(R.id.textInput);
        textInput.setOnEditorActionListener(this);
        textInput.addTextChangedListener(this);

        textOutput = (TextView) view.findViewById(R.id.textOutput);

        buttonTranslate = (ImageButton) view.findViewById(R.id.buttonTranslate);
        buttonTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translate(false);
            }
        });

        buttonFavorites = (ToggleButton) view.findViewById(R.id.buttonFavorites);
        buttonFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TranslaterModel.getIstance().putInFavorites(currentTranslateDataItem, !currentTranslateDataItem.inFavorites);
                currentTranslateDataItem.inFavorites = !currentTranslateDataItem.inFavorites;
                buttonFavorites.setChecked(currentTranslateDataItem.inFavorites);
            }
        });

        buttonClear = (ImageButton) view.findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textInput.setText("");
                buttonClear.setEnabled(false);
                saveInputToSharedPreferences();
            }
        });

        ImageButton buttonSwapLanguages = (ImageButton) view.findViewById(R.id.buttonSwapLanguages);
        buttonSwapLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               swapTranslateLanguages();
            }
        });

        spinnerTranslateFrom = (Spinner) view.findViewById(R.id.spinnerLanguageFrom);
        spinnerTranslateFrom.setOnItemSelectedListener(this);

        spinnerTranslateTo = (Spinner) view.findViewById(R.id.spinnerLanguageTo);
        spinnerTranslateTo.setOnItemSelectedListener(this);

        TextView licenseLinkText = (TextView) view.findViewById(R.id.translateLicenseLinkText);
        licenseLinkText.setMovementMethod(LinkMovementMethod.getInstance());
        licenseLinkText.setText(Html.fromHtml(getResources().getString(R.string.yandex_api_license_link)));

        refreshLanguagesSpinners();
        refreshTranslate(false);
    }

    /**
     * Обновляет состояние экрана. Сеттит перевод (или исходный текст если перешли из истории/избранного), спиннеры языков
     *
     * @param setFromLanguageSpinner устанавливать ли спиннер с какого языка был перевод. Нужно при переходе из истории/избранного.
     *
     * */
    public void refreshTranslate(Boolean setFromLanguageSpinner)
    {
        if (!isAdded())
            return;

        /**
         * 3 стейта:
         * 1) Начальный. Сеттим сохраненные данные из SharedPreferences
         * 2) Пришел результат перевода из апи, либо из истории/избранного. Сеттим новые данные.
         * 3) Всё уже засеччено в один из предыдущих вызовов. Игнорируем.
         * */

        if(TranslaterModel.getIstance().getTranslateDataItem() == null)
        {
            // 1) Начальный. Сеттим из SharedPreferences:
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            translateCheckHandler.removeCallbacks(delayedTranslateRunnable);

            textInput.setText(sharedPreferences.getString("TEXT_INPUT", ""));
            textOutput.setText("");
        }
        else if(TranslaterModel.getIstance().getTranslateDataItem() != currentTranslateDataItem)
        {
            // 2) Есть новые данные. Сеттим их:
            currentTranslateDataItem = TranslaterModel.getIstance().getTranslateDataItem();
            textInTranslate = currentTranslateDataItem.sourceText;

            // сохраняем положение каретки(критично при вводе текста)
            int caretIndex = textInput.getSelectionStart();
            textInput.setText(currentTranslateDataItem.sourceText);
            textInput.setSelection(Math.min(textInput.length(), caretIndex));

            setSpinnersFromCurrentTranslateDataItem(setFromLanguageSpinner);

            textOutput.setText(currentTranslateDataItem.resultText);

            buttonTranslate.setEnabled(false);
        }
        else
        {
            // Никаких изменений. На всякий пожарный выключаем кнопку перевода:
            buttonTranslate.setEnabled(false);
        }

        buttonFavorites.setChecked(currentTranslateDataItem != null ? currentTranslateDataItem.inFavorites : false);
    }

    public void refreshLanguagesSpinners()
    {
        if (!isAdded())
            return;

        if(translateFromSpinnerAdapter == null && TranslaterModel.getIstance().getLanguagesNamesFrom().size() != 0) {
            translateFromSpinnerAdapter = new ArrayAdapter<String>(
                    getContext(),
                    R.layout.spinner_from_language_item,
                    TranslaterModel.getIstance().getLanguagesNamesFrom());
            translateFromSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        if(translateFromSpinnerAdapter != null) {
            spinnerTranslateFrom.setAdapter(translateFromSpinnerAdapter);
            int fromPosition = Math.max(0, TranslaterModel.getIstance().getLanguagesCodesFrom().indexOf(TranslaterModel.getIstance().translationFromLanguage));
            spinnerTranslateFrom.setSelection(fromPosition);
        }

        if(translateToSpinnerAdapter == null && TranslaterModel.getIstance().getLanguagesNamesTo().size() != 0)
        {
            translateToSpinnerAdapter = new ArrayAdapter<String>(
                    getContext(),
                    android.R.layout.simple_spinner_item,
                    TranslaterModel.getIstance().getLanguagesNamesTo());
            translateToSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerTranslateTo.setSelection(translateToSpinnerAdapter.getPosition(TranslaterModel.getIstance().translationToLanguage));
        }

        if(translateToSpinnerAdapter != null) {
            spinnerTranslateTo.setAdapter(translateToSpinnerAdapter);
            int toPosition = Math.max(0, TranslaterModel.getIstance().getLanguagesCodesTo().indexOf(TranslaterModel.getIstance().translationToLanguage));
            spinnerTranslateTo.setSelection(toPosition);
        }
    }

    public void refreshFavoritesButton() {
        if(isAdded())
            buttonFavorites.setChecked(currentTranslateDataItem != null ? currentTranslateDataItem.inFavorites : false);
    }

    private void setSpinnersFromCurrentTranslateDataItem(Boolean setFromLanguageSpinner)
    {
        if(currentTranslateDataItem == null)
            return;

        //Log.d("translaterApp" + getClass().toString(), "до " + TranslaterModel.getIstance().getLanguagesCodesFrom().get(lastSpinnerTranslateFromPosition) + "   " + TranslaterModel.getIstance().getLanguagesCodesTo().get(lastSpinnerTranslateToPosition));

        int translationFromLanguageIndex = -1;
        if(setFromLanguageSpinner) {
            if(currentTranslateDataItem.languageFrom != null) {
                String translationFromLanguage = TranslaterModel.getIstance().getLanguagesCodesFrom().get((int) spinnerTranslateFrom.getSelectedItemId());
                // Если у юзера стоит автоопределении языка не будем юзеру пересеччивать этот спиннер, чтобы ему не пришлось каждый раз возвращать его обратно.
                if(translationFromLanguage != "auto") {
                    translationFromLanguageIndex = TranslaterModel.getIstance().getLanguagesCodesFrom().indexOf(currentTranslateDataItem.languageFrom);
                    if(translationFromLanguageIndex != -1) {
                        lastSpinnerTranslateFromPosition = translationFromLanguageIndex;
                        spinnerTranslateFrom.setSelection(translationFromLanguageIndex);
                    }
                }
            }
        }

        int translationToLanguageIndex = -1;
        if(currentTranslateDataItem.languageTo != null) {
            translationToLanguageIndex = TranslaterModel.getIstance().getLanguagesCodesTo().indexOf(currentTranslateDataItem.languageTo);
            if(translationToLanguageIndex != -1) {
                lastSpinnerTranslateToPosition = translationToLanguageIndex;
                spinnerTranslateTo.setSelection(translationToLanguageIndex);
            }
        }

        //Log.d("translaterApp" + getClass().toString(), "после " + TranslaterModel.getIstance().getLanguagesCodesFrom().get(lastSpinnerTranslateFromPosition) + "   " + TranslaterModel.getIstance().getLanguagesCodesTo().get(lastSpinnerTranslateToPosition));
    }

    /**
     * Сохранение текущих введенных данных в SharedPreferences для возможности воостановления их при следующем запуске
     * */
    private void saveInputToSharedPreferences()
    {
        if(!isAdded())
            return;

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();

        // В мелком проекте без перспективы развития нет смысла выносить текстовые константы или работу с SharedPreferences в отдельный класс. А так да.
        editor.putString("TEXT_INPUT", textInput.getText().toString().trim());
        editor.putString("TRANSLATE_FROM", TranslaterModel.getIstance().translationFromLanguage);
        editor.putString("TRANSLATE_TO", TranslaterModel.getIstance().translationToLanguage);
        editor.apply();
    }

    private void translate(Boolean withClean)
    {
        if (!isAdded())
            return;

        String text = textInput.getText().toString().trim();

        if(withClean) {
            currentTranslateDataItem = null;
            textInTranslate = null;
            translateCheckHandler.removeCallbacks(delayedTranslateRunnable);
        }

        if(text.length() == 0) {
            translateCheckHandler.removeCallbacks(delayedTranslateRunnable);
            return;
        }

        if(currentTranslateDataItem != null && currentTranslateDataItem == TranslaterModel.getIstance().getTranslateDataItem()) {
            translateCheckHandler.removeCallbacks(delayedTranslateRunnable);
            return;
        }

        if(textInTranslate == text)
            return;

        saveInputToSharedPreferences();

        textInTranslate = text;
        TranslaterModel.getIstance().translate(text, TranslaterModel.getIstance().translationFromLanguage, TranslaterModel.getIstance().translationToLanguage);
        //Log.d("translaterApp", " translate " + text);
    }

    /**
     * Переводим и закрываем клавиатуру по энтеру:
     * */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            translateCheckHandler.removeCallbacks(delayedTranslateRunnable);
            translate(false);

            if(getView() != null)
                TranslaterModel.getIstance().closeKeyBoard(getView().getWindowToken());

            return true;
        }
        return false;
    }

    /**
     * Смена направления перевода языков, исключая вариант когда выбрано автоопределение
     **/
    private void swapTranslateLanguages()
    {
        String translationFromLanguage = TranslaterModel.getIstance().getLanguagesCodesFrom().get((int) spinnerTranslateFrom.getSelectedItemId());
        if(translationFromLanguage.equals("auto"))
            return;

        String translationToLanguage = TranslaterModel.getIstance().getLanguagesCodesTo().get((int) spinnerTranslateTo.getSelectedItemId());

        int translationToLanguageIndex = TranslaterModel.getIstance().getLanguagesCodesTo().indexOf(translationFromLanguage);
        int translationFromLanguageIndex = TranslaterModel.getIstance().getLanguagesCodesFrom().indexOf(translationToLanguage);

        // Такие проверки многие в реальных проектах пропускают (проверка не особо нужна только пока список направлений переводов языков будет одинаков, но всё может измениться)
        if(translationFromLanguageIndex == -1 && translationFromLanguageIndex == -1)
            return;

        TranslaterModel.getIstance().translationFromLanguage = translationToLanguage;
        TranslaterModel.getIstance().translationToLanguage = translationFromLanguage;

        lastSpinnerTranslateFromPosition = translationFromLanguageIndex;
        lastSpinnerTranslateToPosition = translationToLanguageIndex;
        spinnerTranslateFrom.setSelection(translationFromLanguageIndex);
        spinnerTranslateTo.setSelection(translationToLanguageIndex);

        translate(true);
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        // В реальной разработке в случае вставки текста из буфера нужно зачистить текст от html-я,
        // чтобы api переводчика его не пыталось перевести.

        // лишние пробелы по краям не должны инициировать перевод равно как и сохраняться в результатах
        String text = textInput.getText().toString().trim();

        if(currentTranslateDataItem != null)
        {
            if(!text.equals(currentTranslateDataItem.sourceText)) {
                currentTranslateDataItem = null;
                textInTranslate = null;
            }
            else {
                return;
            }
        }

        translateCheckHandler.removeCallbacks(delayedTranslateRunnable);

        if(text.length() == 0)
        {
            textOutput.setText("");
            buttonFavorites.setChecked(false);
            buttonClear.setEnabled(false);
            buttonTranslate.setEnabled(false);
        }
        else
        {
            buttonTranslate.setEnabled(true);
            buttonClear.setEnabled(true);

            // разное время таймаута на перевод в зависимости от длины текста. Чтобы не мучать апи и сеть.
            int translateTimeout;
            if (text.length() < 30)
                translateTimeout = 700;
            else if(text.length() < 100)
                translateTimeout = 1000;
            else if(text.length() < 500)
                translateTimeout = 1500;
            else
                translateTimeout = 3000;

            translateCheckHandler.postDelayed(delayedTranslateRunnable, translateTimeout);
            //translate();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
    {
        if(parentView == spinnerTranslateFrom)
        {
            if(lastSpinnerTranslateFromPosition == position)
                return;

            lastSpinnerTranslateFromPosition = position;

            String newTranslationFromLanguage = TranslaterModel.getIstance().getLanguagesCodesFrom().get(position);

            if(TranslaterModel.getIstance().translationFromLanguage != newTranslationFromLanguage)
            {
                TranslaterModel.getIstance().translationFromLanguage = newTranslationFromLanguage;
                translate(true);
            }
        }
        else
        {
            if(lastSpinnerTranslateToPosition == position)
                return;

            lastSpinnerTranslateToPosition = position;

            String newTranslationToLanguage = TranslaterModel.getIstance().getLanguagesCodesTo().get(position);

            if(TranslaterModel.getIstance().translationToLanguage != newTranslationToLanguage)
            {
                TranslaterModel.getIstance().translationToLanguage = newTranslationToLanguage;
                translate(true);
            }
        }
    }

    Runnable delayedTranslateRunnable = new Runnable() {
        @Override
        public void run() {
            translate(false);
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        if(translateCheckHandler != null)
            translateCheckHandler.removeCallbacks(delayedTranslateRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(translateCheckHandler != null)
            translateCheckHandler.removeCallbacks(delayedTranslateRunnable);
    }

    @Override
    public void onStop() {
        saveInputToSharedPreferences();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTranslate(true);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parentView) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
