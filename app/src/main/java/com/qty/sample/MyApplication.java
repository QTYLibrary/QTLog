package com.qty.sample;

import android.app.Application;

import com.qty.log.QTLogManager;
import com.qty.log.bean.QTLogLevel;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        QTLogManager.getInstance().init(getApplicationContext(), "QTLogSample", QTLogLevel.ALL_LEVEL);
    }
}
