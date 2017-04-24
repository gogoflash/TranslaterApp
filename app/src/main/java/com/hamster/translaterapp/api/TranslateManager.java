package com.hamster.translaterapp.api;

import com.hamster.translaterapp.api.yandex.YandexLanguagesListService;
import com.hamster.translaterapp.api.yandex.YandexTranslateService;
import com.hamster.translaterapp.api.yandex.YandexTranslaterLanguagesResponseJSON;
import com.hamster.translaterapp.api.yandex.YandexTranslaterResponseJSON;
import com.hamster.translaterapp.data.TranslateDataItem;
import com.hamster.translaterapp.data.TranslaterModel;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ПК on 11.04.2017.
 *
 */
public class TranslateManager implements Callback<YandexTranslaterResponseJSON> {

    private Retrofit retrofit;

    private static final String API_KEY_YANDEX = "trnsl.1.1.20170405T164546Z.927ade72a74e3dde.fa3a85a19ee0ef151eb43fca91feb6a6cf9600f6";

    private YandexTranslateService yandexService;

    private YandexLanguagesListService yandexLanguagesListService;

    private Call<YandexTranslaterResponseJSON> currentCall;

    private RequestHolder requestHolder;

    public TranslateManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://translate.yandex.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        yandexService = retrofit.create(YandexTranslateService.class);
        yandexLanguagesListService = retrofit.create(YandexLanguagesListService.class);
    }

    public void requestTranslate(String text, String languageCode)
    {
        if(text == null || text.equals(""))
            return;

        text = text.trim();

        //Log.d("translaterApp", "requestTranslate: " + text + " " + languageCode);

        // отменяем предыдущий запрос, если тот еще не выполнился:
        if(currentCall != null)
        {
            //Log.d("translaterApp", "isExecuted: " + String.valueOf(currentCall.isExecuted()) + " isCanceled: " + String.valueOf(currentCall.isCanceled()));
            if(!currentCall.isExecuted() && !currentCall.isCanceled()) {
                currentCall.cancel();
                //currentCall = null;
            }
        }

        // новый запрос:
        currentCall = yandexService.search(API_KEY_YANDEX, text, languageCode, null, null);
        requestHolder = new RequestHolder(currentCall, text, languageCode);
        currentCall.enqueue(this);
    }

    public void requestGetLanguagesList()
    {
        String languageCode = Locale.getDefault().getLanguage();
        Call<YandexTranslaterLanguagesResponseJSON> call = yandexLanguagesListService.search(API_KEY_YANDEX, languageCode);
        call.enqueue(new LanguagesListResponseHandler());
    }

    @Override
    public void onResponse(Call<YandexTranslaterResponseJSON> call, Response<YandexTranslaterResponseJSON> response)
    {
        if(response.body() == null) {
            // Ошибка в ответе от API:
            if(response.errorBody() != null) {
                try {
                    TranslaterModel.getIstance().showErrorAlert("Ошибка запроса в API", "Текст ошибки: \n" + response.errorBody().string());
                    return;
                } catch (IOException e) {
                    //e.printStackTrace();
                    // ошибка без описания со стороны апи прокинется ниже:
                }
            }

            TranslaterModel.getIstance().showErrorAlert("Ошибка запроса в API", "Что-то пошло не так. Отсутствуют данные в ответе от API.");
            return;
        }

        List<String> textList = response.body().getText();

        // если сервис прислал пустой текст намеренно сделаем вид, что ничего не произошло.
        // пусть пробует жмакать на перевод еще. Вероятно в следующий раз сработает.
        if(textList.size() == 0)
            return;

        if(requestHolder.call.equals(call))
        {
            String[] lang = response.body().getLang().split("-");
            // защита на случай присыла сервисом пустышки в поле направления языка перевода. Смысла падать приложению нет равно как и пугать юзера алертом.
            if(lang.length == 0)
                lang[0] = "";

            TranslateDataItem item = new TranslateDataItem(
                    requestHolder.text,
                    textList.get(0),
                    Calendar.getInstance().getTime().getTime(),
                    lang.length > 1 ? lang[0] : "auto",
                    lang.length > 1 ? lang[1] : lang[0],
                    "yandex"
                    );

            // Пишем в базу, инициируем обновление экрана с переводом:
            TranslaterModel.getIstance().setTranslate(item, true, false, false);
        }
    }

    @Override
    public void onFailure(Call<YandexTranslaterResponseJSON> call, Throwable t) {
        if (call.isCanceled())
            TranslaterModel.getIstance().showErrorAlert("Ошибка запроса в API", "Что-то пошло не так. Запрос был отклонен.");
        else
            TranslaterModel.getIstance().showErrorAlert("Ошибка запроса в API", "Что-то пошло не так. Вероятно отсутствует интернетик.");
    }

    class RequestHolder {
        public RequestHolder(Call<YandexTranslaterResponseJSON> call, String text, String lang) {
            this.call = call;
            this.text = text;
            this.lang = lang;
        }

        public Call<YandexTranslaterResponseJSON> call;
        public String text;
        public String lang;
    }

    class LanguagesListResponseHandler implements Callback<YandexTranslaterLanguagesResponseJSON> {

        @Override
        public void onResponse(Call<YandexTranslaterLanguagesResponseJSON> call, Response<YandexTranslaterLanguagesResponseJSON> response) {
            if(response.body() == null) {
                TranslaterModel.getIstance().showErrorAlert("Ошибка запроса в API", "Что-то пошло не так. Не могу получить список поддерживаемых языков.");
                return;
            }

            TranslaterModel.getIstance().setLanguagesList(response.body().getLangs());
        }

        @Override
        public void onFailure(Call<YandexTranslaterLanguagesResponseJSON> call, Throwable t) {
            TranslaterModel.getIstance().showErrorAlert("Ошибка запроса в API", "Что-то пошло не так. Не могу получить список поддерживаемых языков.");
        }
    }
}
