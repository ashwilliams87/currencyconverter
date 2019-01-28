package com.ashwilliams87.currencyconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ashwilliams87.currencyconverter.answers.UsdRates;
import com.ashwilliams87.currencyconverter.interactors.UsdRatesLoaderInteractor;
import com.ashwilliams87.currencyconverter.interfaces.MainContract;
import com.ashwilliams87.currencyconverter.presenters.MainPresenter;
import com.ashwilliams87.currencyconverter.interactors.UsdRatesUpdaterInteractor;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private static final String TAG = "MainActivity";

    private MainContract.Presenter mainPresenter;
    private UsdRates rates = null;
    private TextView messageTextView;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private ArrayAdapter adapterSpinnerFrom;
    private ArrayAdapter adapterSpinnerTo;
    private EditText sumToConvertEditText;
    private EditText sumResultedEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerFrom = findViewById(R.id.currency_convert_from_spinner);
        spinnerTo = findViewById(R.id.currency_convert_to_spinner);
        sumToConvertEditText = findViewById(R.id.currency_convert_from_edit_text);
        sumResultedEditText = findViewById(R.id.currency_converted_to_edit_text);
        sumResultedEditText.setEnabled(false);
        mainPresenter = new MainPresenter(this, new UsdRatesLoaderInteractor(), new UsdRatesUpdaterInteractor());
        messageTextView = findViewById(R.id.currency_message_text_view);

        adapterSpinnerFrom = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapterSpinnerFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapterSpinnerFrom);

        adapterSpinnerTo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapterSpinnerTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTo.setAdapter(adapterSpinnerFrom);


        //конвертировать будем все валюты через Доллар
        //получаем пустой объект курсов Доллара к другим валютам
        //если это самый первый запуск, то загружаем из интернета курсы валют по API
        //если не получится получить через API
        //стартанем новое активити с просьбой подключить интернет
        //Прошу обратить внимание, что то API которое я использую на выходных отдает курс доллара с пятницы
        rates = getUsdRatesSharedPreferences();
        if (rates.getRates().size() == 0) {
            //загрузка через интерактор в отдельном потоке
            //когда заканчивается вызывается методы MainActivity
            //которые заменяют поле rates новым массивом и кладут в кэш новые курсы доллара
            mainPresenter.loadUsdRates();
            sumResultedEditText.setEnabled(false);
            spinnerFrom.setEnabled(false);
            spinnerTo.setEnabled(false);
            messageTextView.setText("Загрузка...");
        } else {
            //если список валют уже получен ранее, прикрепляем через адаптер к спискам
            ArrayList<String> currencyNames = new ArrayList<>(rates.getRates().keySet());

            adapterSpinnerFrom.clear();
            adapterSpinnerFrom.addAll(currencyNames);
            adapterSpinnerFrom.notifyDataSetChanged();

            adapterSpinnerTo.clear();
            adapterSpinnerTo.addAll(currencyNames);
            adapterSpinnerTo.notifyDataSetChanged();
        }

        if (savedInstanceState != null) {
            sumToConvertEditText.setText(savedInstanceState.getString("convertedEditText"));
            spinnerFrom.setSelection(savedInstanceState.getInt("spinnerFromPosition"));
            spinnerTo.setSelection(savedInstanceState.getInt("spinnerToPosition"));
            sumResultedEditText.setText(savedInstanceState.getString("resultConvertedText"));
            this.updateViewElements();
        }

        sumToConvertEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //если есть интернет и курсы валют не сегодняшней даты, попробуем обновить их
                //Данные курсов от ...  пожалуйста подключитесь к интернету, что бы обновить данные курсов.
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String test = rates.getDate();
                boolean cacheIsOld = !Objects.equals(rates.getDate(), currentDate);

                if (isNetworkAvailable() && cacheIsOld) {
                    mainPresenter.updateRates();
                    messageTextView.setText("");
                } else {
                    if (cacheIsOld) {
                        messageTextView.setText("Дата курсов валют:" + rates.getDate() + ". Для обновления курсов пожалуйста подключите интернет.");
                    } else {
                        messageTextView.setText("");
                    }
                }

                //К сожалению не хватает времени реализовывать дальше MVP,
                //конвертирую прямо в UI треде
                MainActivity.this.convertCurrency();

//                if (Looper.myLooper() != Looper.getMainLooper()) {
//                    Log.d("GetFromApi" + "Thread", "NO MainThread!");
//                }
            }
        });

        //        convertButton.setOnClickListener(v -> {
//            //если есть интернет и курсы валют не сегодняшней даты, попробуем обновить их
//            //Данные курсов от ...  пожалуйста подключитесь к интернету, что бы обновить данные курсов.
//            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//            String test = rates.getDate();
//            boolean cacheIsOld = !Objects.equals(rates.getDate(), currentDate);
//
//            if (isNetworkAvailable() && cacheIsOld) {
//                mainPresenter.updateRates();
//                messageTextView.setText("");
//            } else {
//                if (cacheIsOld) {
//                    messageTextView.setText("Дата курсов валют:" + rates.getDate() + ". Для обновления курсов пожалуйста подключите интернет.");
//                } else {
//                    messageTextView.setText("");
//                }
//            }
//
//            //К сожалению не хватает времени реализовывать дальше MVP,
//            //конвертирую прямо в UI треде
//            MainActivity.this.convertCurrency();
//        });


    }

    @SuppressLint("SetTextI18n")
    private void convertCurrency() {
        String sumText = sumToConvertEditText.getText().toString();
        if (!sumText.isEmpty()) {
            //получаем значения из edit и курсы доллара
            String convertFromCurrency = spinnerFrom.getSelectedItem().toString();
            String convertToCurrency = spinnerTo.getSelectedItem().toString();
            TreeMap<String, Double> usdRates = (TreeMap<String, Double>) rates.getRates();

            //конвертируем из текущей валюты в доллар, и потом из доллара по курсу в нужную валюту
            double sum = Double.parseDouble(sumText);
            double usdRateOfCurrentCurrency = Double.parseDouble(String.valueOf(usdRates.get(convertFromCurrency)));
            double usdRateOfPointCurrency = Double.parseDouble(String.valueOf(usdRates.get(convertToCurrency)));
            double sumConvertedInUsd = sum / usdRateOfCurrentCurrency;
            double convertedSum = sumConvertedInUsd * usdRateOfPointCurrency;

            BigDecimal bd = new BigDecimal(convertedSum);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            sumResultedEditText.setText(bd.toString());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setRates(UsdRates rates) {
        this.rates = rates;
    }

    /**
     * метод вызывается из MainPresenter, по итогам получения курсов от UsdRatesLoaderInteractor
     */
    public void saveRatesToSharedPreferences() {
        Log.d(TAG + " setShared", "OK!");
        //сохранияем дату курсов и сами курсы Доллара
        SharedPreferences usdRatesDataShared = this.getSharedPreferences("com.ashwilliams87.first.usdRates.fields", Context.MODE_PRIVATE);
        usdRatesDataShared.edit().putString("date", rates.getDate()).apply();

        SharedPreferences usdRatesShared = this.getSharedPreferences("com.ashwilliams87.first.usdRates.array", Context.MODE_PRIVATE);
        SharedPreferences.Editor keyValuesEditor = usdRatesShared.edit();
        TreeMap<String, Double> usdRates = (TreeMap<String, Double>) rates.getRates();

        for (String s : rates.getRates().keySet()) {
            // use the name as the key, and the icon as the value
            keyValuesEditor.putString(s, Double.toString(usdRates.get(s)));
        }

        keyValuesEditor.apply();
    }

    public UsdRates getUsdRatesSharedPreferences() {
        //загружаем объект usdrates новыми курсами
        Log.d(TAG + "GetShared", "OK!");
        UsdRates usdRates = new UsdRates();

        usdRates.setBase("USD");
        SharedPreferences baseShared = this.getSharedPreferences("com.ashwilliams87.first.usdRates.fields", Context.MODE_PRIVATE);
        usdRates.setDate(baseShared.getString("date", "2000-01-01"));
        SharedPreferences keyValues = this.getSharedPreferences("com.ashwilliams87.first.usdRates.array", Context.MODE_PRIVATE);
        TreeMap<String, Double> keys = new TreeMap<>();
        keys.putAll((Map<? extends String, ? extends Double>) keyValues.getAll());
        Double d = keys.get("USD");

        usdRates.setRates(keys);

        return usdRates;
    }

    @Override
    public void askUserForInternetConnection() {
        Intent intent = new Intent(this, AskInternetActivity.class);
        startActivity(intent);
    }

    @Override
    public void updateViewElements() {
        //включаем кнопки и обновляем адаптеры
        sumResultedEditText.setEnabled(true);
        spinnerFrom.setEnabled(true);
        spinnerTo.setEnabled(true);
        messageTextView.setText("");

        ArrayList<String> currencyNames = new ArrayList<>(rates.getRates().keySet());
        adapterSpinnerFrom.clear();
        adapterSpinnerFrom.addAll(currencyNames);
        adapterSpinnerFrom.notifyDataSetChanged();
        adapterSpinnerTo.clear();
        adapterSpinnerTo.addAll(currencyNames);
        adapterSpinnerTo.notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void sendMessageCacheOld() {
        messageTextView.setText("Дата курсов валют:" + rates.getDate() + ". Для обновления курсов пожалуйста подключите интернет.");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("convertedEditText", sumToConvertEditText.getText().toString());
        outState.putInt("spinnerToPosition", spinnerTo.getSelectedItemPosition());
        outState.putInt("spinnerFromPosition", spinnerFrom.getSelectedItemPosition());
        outState.putString("resultConvertedText", sumResultedEditText.getText().toString());
    }
}
