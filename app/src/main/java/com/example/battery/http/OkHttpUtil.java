package com.example.battery.http;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Saturday on 2023/4/8.
 */

public class OkHttpUtil {

    public static final Gson gson = new Gson();

    static class OkHttpClientSingleton {
        private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public static <T> void post(String url, T requestBody, Callback callback) {

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), gson.toJson(requestBody)))
                .build();
        Log.v("battery_post", gson.toJson(requestBody));

        OkHttpClientSingleton.okHttpClient.newCall(request).enqueue(callback);
    }

    public static void get(String url, Map<String, String> params, Callback callback) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                urlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build().url())
                .build();

        OkHttpClientSingleton.okHttpClient.newCall(request).enqueue(callback);
    }
}
