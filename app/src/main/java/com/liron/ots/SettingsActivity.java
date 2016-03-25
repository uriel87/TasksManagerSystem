package com.liron.ots;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.parse.ParseUser;

/**
 * Created by user on 08/03/2016.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
//        // Vibrate for 500 milliseconds
//        v.vibrate(500);

        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

//        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);


//        int newTimeSet = 5;
//        if (PreferenceManager.getDefaultSharedPreferences(this).getString(TasksDbConsts.SET_TIME_NOTIFY, "5").equals(null)) {
//            newTimeSet = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(TasksDbConsts.SET_TIME_NOTIFY, "5"));
//        }


//        SharedPreferences sharedpreferences = getSharedPreferences(TasksDbConsts.SharedPrefsName, Context.MODE_PRIVATE);
//        int newTimeSet_ = sharedpreferences.getInt(TasksDbConsts.SET_TIME_NOTIFY, 5);
//
//        PreferenceScreen pScreen;
//        Preference pref = pScreen.getPreference(i);
//        EditTextPreference etPref = (EditTextPreference) pref;
//        pref.setSummary(newTimeSet_);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

            int newTimeSet = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("setTimeNotify", "5"));
            Preference connectionPref = findPreference("setTimeNotify");
            connectionPref.setSummary("The time now is: " + newTimeSet);
            sendNewTaskNotification(newTimeSet);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("setTimeNotify")) {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary("The time now is: " + sharedPreferences.getString(key, ""));
                sendNewTaskNotification(Integer.parseInt(sharedPreferences.getString(key,"5")));
            }
        }

        public void sendNewTaskNotification(int setTime) {
            int REQUEST_CODE = 234;
            Intent i = new Intent("ots.tasks_check_broadcast");

            ParseUser currentUser = ParseUser.getCurrentUser();
            long refreshTime = 1000 * setTime * 60;
            Log.i("alaram preferance", "alaram preferance" + setTime);

            cancelAlarmIfExists(getActivity(), REQUEST_CODE, i);

            PendingIntent sender = PendingIntent.getBroadcast(getActivity(), REQUEST_CODE, i, 0);
            long firstTime = SystemClock.elapsedRealtime();
            AlarmManager am = (AlarmManager) getActivity()
                    .getSystemService(Context.ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME, firstTime, refreshTime, sender);
        }

        public void cancelAlarmIfExists(Context mContext,int requestCode,Intent intent){
            try{
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent,0);
                AlarmManager am=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
                am.cancel(pendingIntent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
