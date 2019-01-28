package com.ashwilliams87.currencyconverter.threads;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ashwilliams87.currencyconverter.answers.UsdRates;
import com.ashwilliams87.currencyconverter.helpers.RetrofitApp;
import com.ashwilliams87.currencyconverter.interactors.UsdRatesLoaderInteractor;
import com.ashwilliams87.currencyconverter.interactors.UsdRatesUpdaterInteractor;
import com.ashwilliams87.currencyconverter.interfaces.Api;
import com.ashwilliams87.currencyconverter.interfaces.MainContract;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetFromApiUsdRatesThread extends Thread {

    private MainContract.UsdRatesInteractor intercator;


    public GetFromApiUsdRatesThread(MainContract.UsdRatesInteractor intercator) {
        this.intercator = intercator;
    }

    public void run() {
        Log.d("InitExerciseDatabase", "Get Retrofit Models");

        RetrofitApp.getApi().getLatestRates("USD").enqueue(new Callback<UsdRates>() {
            @Override
            public void onResponse(@NonNull Call<UsdRates> call, @NonNull Response<UsdRates> response) {

                if (Looper.myLooper() != Looper.getMainLooper()) {
                    Log.d("GetFromApi" + "Thread", "NO MainThread!");
                }
                //Данные успешно пришли, но надо проверить response.body() на null
                intercator.endLoadUsdRates(Objects.requireNonNull(response.body()));
            }

            public void onFailure(@NonNull Call<UsdRates> call, Throwable t) {
                //Произошла ошибка
                intercator.failLoadUsdRates();
            }
        });
    }


    //        Api retrofit = RetrofitApp.getApi();
//        Response response;
//        UsdRates rates = null;
//
//        try {
//            response = retrofit.getLatestRates("USD").execute();
//            //вариант1
//            rates = (UsdRates) response.body();
//
//            //вариант 2
//            this.intercator.endLoadUsdRates((UsdRates) Objects.requireNonNull(response.body()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (rates == null) {
//            Log.d("InitExerciseDatabase", "null");
//            this.intercator.failLoadUsdRates();
//        } else {
//            this.intercator.endLoadUsdRates(rates);
//        }
}
