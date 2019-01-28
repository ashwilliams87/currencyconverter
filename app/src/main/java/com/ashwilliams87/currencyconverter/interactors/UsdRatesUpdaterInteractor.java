package com.ashwilliams87.currencyconverter.interactors;

import android.support.annotation.NonNull;

import com.ashwilliams87.currencyconverter.answers.UsdRates;
import com.ashwilliams87.currencyconverter.interfaces.MainContract;
import com.ashwilliams87.currencyconverter.threads.GetFromApiUsdRatesThread;

import java.util.Observable;

public class UsdRatesUpdaterInteractor extends Observable implements MainContract.UsdRatesInteractor {

    private static final String TAG = "UsdRatesUpdaterInteractor";
    private UsdRates rates;

    public void loadUsdRates() {
        //к сожалению не остается времени, но этот тред запускается в main, когда я его дебажил
        GetFromApiUsdRatesThread loaderThread = new GetFromApiUsdRatesThread(this);
        loaderThread.start();
    }

    public void endLoadUsdRates(@NonNull UsdRates rates) {
        this.rates = rates;
        this.setChanged();
        notifyObservers(true);
    }

    public UsdRates getUsdRates() {
        return rates;
    }

    @Override
    public void failLoadUsdRates() {
        this.setChanged();
        notifyObservers(false);
    }
}
