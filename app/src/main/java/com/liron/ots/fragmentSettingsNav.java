package com.liron.ots;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.liron.ots.db.TasksDbConsts;
import com.parse.ParseUser;

/**
 * Created by user on 16/01/2016.
 */
public class fragmentSettingsNav extends Fragment {
    private View view;
    private Context context;
    ParseUser mCurrentUser;
    private EditText setTime;
    private Button setTimeBtn;
    SharedPreferences sharedpreferences;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings_nav, container, false);
        init();
        return view;
    }

    public void init() {
        setTime = (EditText) view.findViewById(R.id.set_time_notification_setting);
        setTimeBtn = (Button) view.findViewById(R.id.set_time_button_setting);
        setTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeRefreshNotify();
            }
        });
    }

    protected void ChangeRefreshNotify() {
        String newTimeSetting = setTime.getText().toString();
        if((TextUtils.isEmpty(newTimeSetting)) || newTimeSetting.equals("0")) {
            setTime.setError("Invalid input");
            return;
        }
        int setRefresh = Integer.valueOf(newTimeSetting);
        sharedpreferences = getActivity().getSharedPreferences(TasksDbConsts.SharedPrefsName, Context.MODE_PRIVATE);
        if(sharedpreferences != null) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.clear();
            editor.putInt(TasksDbConsts.SET_TIME_NOTIFY, setRefresh);
            editor.apply();
        }
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
        Toast.makeText(getActivity(), "Settings notification to " + setRefresh + " minuets", Toast.LENGTH_LONG).show();
    }

}
