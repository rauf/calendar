package com.company.calendar;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by abdul on 27-Jun-17.
 */

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
