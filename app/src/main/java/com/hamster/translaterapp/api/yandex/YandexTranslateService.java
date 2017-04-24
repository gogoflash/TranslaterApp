package com.hamster.translaterapp.api.yandex;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by ПК on 05.04.2017.
 */
public interface YandexTranslateService {
    @POST("api/v1.5/tr.json/translate")
    Call<YandexTranslaterResponseJSON> search(
            @Query("key") String key,
            @Query("text") String text,
            @Query("lang") String lang,
            @Query("format") String format,
            @Query("options") String options
    );
}