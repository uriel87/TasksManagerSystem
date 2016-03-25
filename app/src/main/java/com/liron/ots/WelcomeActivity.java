package com.liron.ots;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseUser;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button mSignUpButton = (Button) findViewById(R.id.wdone);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.put("is_active", true);
            String wteam = currentUser.getString("team");
            String wmanager = currentUser.getString("manager_name");
            TextView mytext = (TextView)findViewById(R.id.mytext);
            mytext.setText("Hello!\n" + "you have been added to team"+ " " + wteam+ " " + "by" + " " + wmanager + ".");

            currentUser.saveInBackground();
        }

    }
}
