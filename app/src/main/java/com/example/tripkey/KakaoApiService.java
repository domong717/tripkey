package com.example.tripkey;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface KakaoApiService {

    @GET("v2/local/search/category.json")
    Call<KakaoSearchResponse> searchPlacesByCategory(
            @Header("Authorization") String authorization,
            @Query("category_group_code") String categoryGroupCode,
            @Query("x") double longitude,
            @Query("y") double latitude,
            @Query("radius") int radius,
            @Query("size") int size
    );

    @GET("v2/local/search/keyword.json")
    Call<KakaoSearchResponse> searchKeyword(
            @Header("Authorization") String auth,
            @Query("query") String query
    );

}
