package com.maya.newbulgariankeyboard.webclient;


import com.maya.newbulgariankeyboard.gif_model.AppGifModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LatestGifEndpoints {


    @GET("gifs/trending")
    Call<AppGifModel> getTrendingGifsFromGiphy(@Query("api_key") String api_key,
                                               @Query("limit") int limit);

    @GET("gifs/search")
    Call<AppGifModel> getSearchedGifsFromGiphy(@Query("q") String q,
                                               @Query("api_key") String api_key,
                                               @Query("limit") int limit);

    @GET("stickers/trending")
    Call<AppGifModel> getTrendingStickersFromGiphy(@Query("api_key") String api_key,
                                                       @Query("limit") int limit);

    @GET("stickers/search")
    Call<AppGifModel> getSearchedStickersFromGiphy(@Query("q") String q,
                                               @Query("api_key") String api_key,
                                               @Query("limit") int limit);
}
