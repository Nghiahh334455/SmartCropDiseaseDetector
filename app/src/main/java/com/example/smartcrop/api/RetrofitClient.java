package com.example.smartcrop.api;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofitAi = null;
    private static Retrofit retrofitSql = null;

    // AI chạy ở cổng 8000 (VS Code)
    public static final String AI_URL = "http://127.0.0.1:8000";
    // SQL chạy ở cổng 8001 (Android Studio)
    public static final String SQL_URL = "http://127.0.0.1:8001";

    private static OkHttpClient getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
    }

    public static ApiService getAiService() {
        if (retrofitAi == null) {
            retrofitAi = new Retrofit.Builder()
                    .baseUrl(AI_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient())
                    .build();
        }
        return retrofitAi.create(ApiService.class);
    }

    public static ApiService getSqlService() {
        if (retrofitSql == null) {
            retrofitSql = new Retrofit.Builder()
                    .baseUrl(SQL_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient())
                    .build();
        }
        return retrofitSql.create(ApiService.class);
    }
}
