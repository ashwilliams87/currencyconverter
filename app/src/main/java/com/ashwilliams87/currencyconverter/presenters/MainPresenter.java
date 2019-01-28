package com.ashwilliams87.currencyconverter.presenters;

import android.util.Log;

import com.ashwilliams87.currencyconverter.answers.UsdRates;
import com.ashwilliams87.currencyconverter.interactors.UsdRatesUpdaterInteractor;
import com.ashwilliams87.currencyconverter.interactors.UsdRatesLoaderInteractor;
import com.ashwilliams87.currencyconverter.interfaces.MainContract;

import java.util.Observable;
import java.util.Observer;

public class MainPresenter implements MainContract.Presenter, Observer {

    private static final String TAG = "MainPresenter";

    private MainContract.View mainActivity;

    private UsdRatesUpdaterInteractor usdRatesUpdaterInteractor;
    private UsdRatesLoaderInteractor usdRatesLoaderInteractor;

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (usdRatesUpdaterInteractor != null) {
            usdRatesUpdaterInteractor.deleteObserver(this);
            usdRatesUpdaterInteractor = null;
        }
        if (usdRatesLoaderInteractor != null) {
            usdRatesLoaderInteractor.deleteObserver(this);
            usdRatesLoaderInteractor = null;
        }

    }

    @Override
    public void loadUsdRates() {
        Log.d(TAG, "onClickConvertButton");
        usdRatesLoaderInteractor.addObserver(this);
        usdRatesLoaderInteractor.loadUsdRates();
    }

    @Override
    public void updateRates() {
        Log.d(TAG, "onClickConvertButton");
        usdRatesUpdaterInteractor.addObserver(this);
        usdRatesUpdaterInteractor.loadUsdRates();

    }

    public MainPresenter(MainContract.View mainActivity, UsdRatesLoaderInteractor usdRatesLoaderInteractor, UsdRatesUpdaterInteractor usdRatesUpdaterInteractor) {
        this.usdRatesUpdaterInteractor = usdRatesUpdaterInteractor;
        this.usdRatesLoaderInteractor = usdRatesLoaderInteractor;
        this.mainActivity = mainActivity;
    }

    @Override
    public void update(Observable observable, Object o) {

        Log.d(TAG, "update");
        //различается потому что при первой загрузке нужно перекинуть пользователя на другой экран
        //и просить интернет, что бы получить хоть какие-то курсы валют
        if (observable instanceof UsdRatesLoaderInteractor) {
            UsdRatesLoaderInteractor usdRatesLoaderInteractor = (UsdRatesLoaderInteractor) observable;
            if (!((Boolean) o)) {
                mainActivity.askUserForInternetConnection();
            } else {
                UsdRates rates = usdRatesLoaderInteractor.getUsdRates();
                mainActivity.setRates(rates);
                mainActivity.saveRatesToSharedPreferences();
                mainActivity.updateViewElements();
                //mainActivity.getUsdRatesSharedPreferences();
            }

        }
        //обновление курсов, если дата кэша не совпадает с текущей, вызов из MainActivity
        if (observable instanceof UsdRatesUpdaterInteractor) {
            UsdRatesUpdaterInteractor usdRatesLoaderInteractor = (UsdRatesUpdaterInteractor) observable;
            if (!((Boolean) o)) {
                mainActivity.sendMessageCacheOld();
            } else {
                UsdRates rates = usdRatesLoaderInteractor.getUsdRates();
                mainActivity.setRates(rates);
                mainActivity.saveRatesToSharedPreferences();
                mainActivity.updateViewElements();
            }


        }


    }
}
