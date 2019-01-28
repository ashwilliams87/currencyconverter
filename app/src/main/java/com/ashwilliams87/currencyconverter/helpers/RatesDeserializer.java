package com.ashwilliams87.currencyconverter.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ashwilliams87.currencyconverter.answers.UsdRates;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;


import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class RatesDeserializer implements JsonDeserializer<UsdRates> {

    private static final String KEY_URI = "base";
    private static final String KEY_METHOD = "date";
    private static final String KEY_PARAMETERS = "rates";

    @Override
    public UsdRates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        // Read simple String values.
        final String base = jsonObject.get(KEY_URI).getAsString();
        final String date = jsonObject.get(KEY_METHOD).getAsString();

        // Здесь мы читаем поле rates у овтета сервера и сортируем
        final Map<String, Double> rates = readParametersMap(jsonObject);

        UsdRates result = new UsdRates();
        result.setBase(base);
        result.setDate(date);
        result.setRates(rates);
        return result;
    }


    @Nullable
    private Map<String, Double> readParametersMap(@NonNull final JsonObject jsonObject) {
        //десереализуем поля gson
        final JsonElement paramsElement = jsonObject.get(KEY_PARAMETERS);
        if (paramsElement == null) {
            // value not present at all, just return null
            return null;
        }

        final JsonObject parametersObject = paramsElement.getAsJsonObject();
        final Map<String, Double> rates = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : parametersObject.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue().getAsDouble();
            rates.put(key, value);
        }


        return rates;
    }
}