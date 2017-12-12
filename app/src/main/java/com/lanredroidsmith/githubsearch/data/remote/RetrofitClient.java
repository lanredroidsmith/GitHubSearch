package com.lanredroidsmith.githubsearch.data.remote;

import com.google.gson.GsonBuilder;
import com.lanredroidsmith.githubsearch.util.MainUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Lanre on 11/6/17.
 */

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(MainUtils.BASE_URL)
                    .addConverterFactory(GsonConverterFactory
                            .create(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                    .client(new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build())
                    .build();
        }
        return retrofit;
    }
}