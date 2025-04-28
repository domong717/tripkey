package com.example.tripkey.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import com.example.tripkey.BuildConfig;


public interface ApiService {

    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer " + BuildConfig.OPENAI_API_KEY
    })
    @POST("v1/chat/completions")
    public Call<GptResponse> getGptAnswer(@Body GptRequest request);
}