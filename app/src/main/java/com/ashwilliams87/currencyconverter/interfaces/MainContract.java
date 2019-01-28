package com.ashwilliams87.currencyconverter.interfaces;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.ashwilliams87.currencyconverter.answers.UsdRates;

public interface MainContract {

    interface View {

        void setRates(UsdRates rates);

        void saveRatesToSharedPreferences();

        UsdRates getUsdRatesSharedPreferences();

        void askUserForInternetConnection();

        void updateViewElements();

        void sendMessageCacheOld();
    }

    interface Presenter {

        void onDestroy();

        void loadUsdRates();

        void updateRates();
    }

    interface UsdRatesInteractor {

        void loadUsdRates();

        void endLoadUsdRates(@NonNull UsdRates rates);

        UsdRates getUsdRates();

        void failLoadUsdRates();
    }
}
