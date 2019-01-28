package com.ashwilliams87.currencyconverter.helpers;

import android.app.Application;

import com.ashwilliams87.currencyconverter.answers.UsdRates;
import com.ashwilliams87.currencyconverter.interfaces.Api;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitApp extends Application {

    private static Api currencyApi;
    private Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();
        //https://api.exchangeratesapi.io/latest?base=USD
        //https://ratesapi.io/api/latest?base=USD
        //https://oplotbot.ashwilliams.tk/api/latest?base=USD
        //к сожалению обнаружил, что найденное мной API может внезапно отдавать 502 ошибку
        //это не приятно если внезапно приложение не сможет даже в первый раз получить данные
        //прокинул через свой сервер, в первый раз гарантировано получит курс
        retrofit = new Retrofit.Builder()
                .baseUrl("https://oplotbot.ashwilliams.tk") //Базовая часть адреса
                .addConverterFactory(createGsonConverter())
                //.addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        currencyApi = retrofit.create(Api.class); //Создаем объект, при помощи которого будем выполнять запросы
    }

    private Converter.Factory createGsonConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(UsdRates.class, new RatesDeserializer());
        Gson gson = gsonBuilder.create();
        return GsonConverterFactory.create(gson);
    }

    public static Api getApi() {
        return currencyApi;
    }
}