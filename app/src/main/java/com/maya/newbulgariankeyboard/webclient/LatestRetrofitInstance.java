package com.maya.newbulgariankeyboard.webclient;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LatestRetrofitInstance {

    private static final String BASE_URL_MUSIK = "https://api.giphy.com/v1/";
    private static Retrofit musikRetrofitInstance;

    public static Retrofit getRetrofitAppInstance(Context context) {
        if (musikRetrofitInstance == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            musikRetrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL_MUSIK)
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return musikRetrofitInstance;
    }
}
