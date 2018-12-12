package com.d.fingerprintcompat;

import android.app.Application;

import com.d.lib.fingerprintcompat.FingerprintCompat;

/**
 * App
 * Created by D on 2018/12/12.
 **/
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FingerprintCompat.setDebug(true);
    }
}
