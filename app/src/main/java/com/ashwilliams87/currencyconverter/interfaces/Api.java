package com.ashwilliams87.currencyconverter.interfaces;

import com.ashwilliams87.currencyconverter.answers.UsdRates;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    @GET("/api/latest")
    Call<UsdRates> getLatestRates(@Query("base") String resourceName);
}