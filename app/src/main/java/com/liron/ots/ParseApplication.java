package com.liron.ots;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application
{

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Task.class);
        ParseObject.registerSubclass(OTSUser.class);
        //Parse.enableLocalDatastore(this);
        Parse.initialize(this);
    }
}