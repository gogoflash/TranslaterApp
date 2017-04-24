package com.hamster.translaterapp.api.yandex;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by ПК on 18.04.2017.
 */
public interface YandexLanguagesListService {
    @POST("api/v1.5/tr.json/getLangs")
    Call<YandexTranslaterLanguagesResponseJSON> search(
            @Query("key") String key,
            @Query("ui") String text
    );
}