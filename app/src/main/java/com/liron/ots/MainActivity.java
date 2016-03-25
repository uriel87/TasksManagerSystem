package com.liron.ots;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Context context;
    private Fragment mainFragment;
    private Handler handler = new Handler();
    private Runnable runPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ParseUser mCurrentUser = ParseUser.getCurrentUser();
        if(mCurrentUser != null) {
            if (!mCurrentUser.getBoolean("is_manager")) {
                navigationView.getMenu().findItem(R.id.Add_member).setVisible(false);
            }
        }
        navigationView.setNavigationItemSelectedListener(this);

        setFirstFragment();

        String taskId = getIntent().getStringExtra("task_id");
        ParseUser user = ParseUser.getCurrentUser();

        if ((user != null) && (taskId != null))
        {
            Intent intent;

            if (!user.getBoolean("is_manager"))
            {
                intent = new Intent(this, SaveMemberTaskActivity.class);
            }
            else
            {
                intent = new Intent(this, SaveTaskActivity.class);
            }

            intent.putExtra("task_id", taskId);
            startActivity(intent);
        }

       int newTimeSet = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("setTimeNotify", "5"));
       //Toast.makeText(this, "main activity: " + newTimeSet, Toast.LENGTH_LONG).show();
        //int newTimeSet = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("setTimeNotify", "5"));
//        SharedPreferences connectionPref = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = connectionPref.edit();
//        editor.putString("setTimeNotify", String.valueOf(connectionPref.getString("setTimeNotify", "5")));
//        editor.apply();
//        SettingsActivity.PrefsFragment prefsFragment = new SettingsActivity.PrefsFragment();
//        prefsFragment.sendNewTaskNotification(newTimeSet);
          sendNewTaskNotification(newTimeSet);
//        connectionPref.setSummary("The time now is: " + newTimeSet);
//        sendNewTaskNotification(newTimeSSet);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //refreshTasks(this);
        setFirstFragment();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        handler.removeCallbacks(runPager);
    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = new Fragment();
        int id = item.getItemId();
        switch (id) {
            case R.id.tasks_list:
                fragment = new FragmentUserTasks();
                break;
            case R.id.Add_member:
                fragment = new FragmentUserList();
                break;
            case R.id.Settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.Logout:
                DialogFragment newFragment = FragmentLogoutNav.newInstance(R.string.are_you_sure);
                newFragment.show(getFragmentManager(), "dialog");
                break;
            case R.id.About:
                fragment = new FragmentAboutNav();
                break;
            default:
                fragment = new FragmentUserTasks();
                break;
        }

        if (null != fragment)
        {
            setTitle(item.getTitle().toString());
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.content_frame, fragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void setFirstFragment()
    {
        setTitle("Task list");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.content_frame, new FragmentUserTasks()).addToBackStack(null);
        fragmentTransaction.commit();
        handler.removeCallbacks(runPager);
    }

    public void sendNewTaskNotification(int setTime) {
        int REQUEST_CODE = 234;
        Intent i = new Intent("ots.tasks_check_broadcast");

        ParseUser currentUser = ParseUser.getCurrentUser();
        long refreshTime = 1000 * setTime * 60;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, i,0);
        AlarmManager amc=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        amc.cancel(pendingIntent);

        PendingIntent sender = PendingIntent.getBroadcast(this, REQUEST_CODE, i, 0);
        long firstTime = SystemClock.elapsedRealtime();
        AlarmManager am = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, firstTime, refreshTime, sender);
    }

}
