package com.liveupdatestatus;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by LEV on 28/07/2018.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
