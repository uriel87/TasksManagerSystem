package com.liron.ots.analytic;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.liron.ots.OTSUser;
import com.liron.ots.R;
import com.liron.ots.Task;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by User on 19/03/2016.
 */

public class AnalyticsApplication extends Application {
    private Tracker mTracker;
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Task.class);
        ParseObject.registerSubclass(OTSUser.class);
        //Parse.enableLocalDatastore(this);
        Parse.initialize(this);
    }
    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
