package com.example.tripkey;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KakaoApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://dapi.kakao.com/") // 카카오맵 REST API base url
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
