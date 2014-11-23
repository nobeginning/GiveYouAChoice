package com.yxc.choice;

import android.app.Application;
import net.tsz.afinal.FinalDb;

/**
 * Created by Robin on 2014/11/22.
 */
public class MyApplication extends Application {

    public static FinalDb db;
    public static MyApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        db = FinalDb.create(this);
    }


}
