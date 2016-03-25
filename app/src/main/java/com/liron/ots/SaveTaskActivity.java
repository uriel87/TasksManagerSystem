package com.liron.ots;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.liron.ots.db.TasksProvider;
import com.liron.ots.users.LoginActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SaveTaskActivity extends AppCompatActivity
{
    ParseUser mCurrentUser;
    TextView mTaskNameView;
    RadioGroup mCategoryView;
    RadioGroup mPriorityView;
    Button mDateView;
    Button mTimeView;
    AppCompatSpinner mAssigneeView;
    AppCompatSpinner mLocationView;
    Task task;

    Calendar dueDate;
    ImageView mTaskImage;
    boolean isImageFitToScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        checkUser();
        refreshTeamMembers();
        refreshLocation();

        String taskId = getIntent().getStringExtra("task_id");
        TasksProvider provider = new TasksProvider(this);
        task = provider.query(taskId);

        provider.close();

        if (task != null)
        {
            setViewsValues(task);
        }
    }

    private void setViewsValues(Task task)
    {
        mTaskNameView.setText(task.getName());

        ArrayList<View> btns = new ArrayList<>();
        mCategoryView.findViewsWithText(btns, task.getCategory().name(), View.FIND_VIEWS_WITH_TEXT);
        ((RadioButton)btns.get(0)).setChecked(true);

        btns.clear();
        mPriorityView.findViewsWithText(btns, task.getPriority().name(), View.FIND_VIEWS_WITH_TEXT);
        ((RadioButton)btns.get(0)).setChecked(true);

        dueDate.setTime(new Date(task.getDueDate()));

        // set selected dueDate into textview
        mDateView.setText(new SimpleDateFormat("dd-MM-yyyy").format(dueDate.getTime()));;

        mTimeView.setText(new SimpleDateFormat("HH:mm:ss").format(dueDate.getTime()));

        new DownloadImageTask(mTaskImage).execute(task.getPhoto());
    }

    private void initViews()
    {
        mTaskNameView = (TextView) findViewById(R.id.name);
        mCategoryView = (RadioGroup) findViewById(R.id.category);
        mPriorityView = (RadioGroup) findViewById(R.id.priority);
        mDateView = (Button) findViewById(R.id.date);
        mTimeView = (Button) findViewById(R.id.time);
        mAssigneeView = (AppCompatSpinner) findViewById(R.id.assignee);
        mLocationView = (AppCompatSpinner) findViewById(R.id.location);
        mTaskImage = (ImageView) findViewById(R.id.taskImage);

        // Process to get Current Date
        dueDate = Calendar.getInstance();

        mDateView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(SaveTaskActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected dueDate in textbox
                                dueDate.set(Calendar.YEAR, year);
                                dueDate.set(Calendar.MONTH, monthOfYear);
                                dueDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                // set selected dueDate into textview
                                mDateView.setText(new SimpleDateFormat("dd-MM-yyyy").format(dueDate.getTime()));
                            }
                        }, dueDate.get(Calendar.YEAR), dueDate.get(Calendar.MONTH), dueDate.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            }
        });

        mTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(SaveTaskActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                dueDate.set(Calendar.HOUR, selectedHour);
                                dueDate.set(Calendar.MINUTE, selectedMinute);

                                mTimeView.setText(new SimpleDateFormat("HH:mm").format(dueDate.getTime()));
                            }
                        }, dueDate.get(Calendar.HOUR), dueDate.get(Calendar.MINUTE), true);//Yes 24 hour time
                tpd.show();
            }
        });
    }

    public void onResizeClick(View view){
        if(isImageFitToScreen) {
            isImageFitToScreen=false;
            mTaskImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            mTaskImage.setAdjustViewBounds(true);
        }else{
            isImageFitToScreen=true;
            mTaskImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            mTaskImage.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    private void refreshTeamMembers()
    {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("team", mCurrentUser.getString("team"));
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> members, ParseException e) {
                if (e == null) {
                    Log.d("get_team_members", "Retrieved " + members.size() + " members");
                    ArrayList<String> spinnerArray = new ArrayList<String>();
                    for (ParseUser member : members) {
                        spinnerArray.add(member.getUsername());
                    }

                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                            SaveTaskActivity.this,
                            android.R.layout.simple_spinner_item,
                            spinnerArray.toArray(new String[spinnerArray.size()]));
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mAssigneeView.setAdapter(spinnerArrayAdapter);

                    if (task != null) {
                        mAssigneeView.setSelection(spinnerArrayAdapter.getPosition(task.getAssignee()));
                    }

                } else {
                    Log.d("get_team_members", "Error: " + e.getMessage());
                }
            }
        });
    }
    private void refreshLocation()
    {
// Array of choices
        String colors[] = {"class 200","class 246","class 247","class 248","class 258"};

// Selection of the spinner
        Spinner spinner2 = (Spinner) findViewById(R.id.location);

// Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, colors);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner2.setAdapter(spinnerArrayAdapter2);
    }

    private void checkUser()
    {
        mCurrentUser = ParseUser.getCurrentUser();
        if (mCurrentUser != null)
        {
            // do stuff with the user
        } else
        {
            // show the signup or login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }


    private void sendNewTaskNotification(long taskId, String userName, String taskName) {
        int REQUEST_CODE = 234;
        Intent i = new Intent("com.rtt.reminder_broadcast");
        i.putExtra("taskId", taskId);
        i.putExtra("userName", userName);
        i.putExtra("taskName", taskName);
        PendingIntent sender = PendingIntent.getBroadcast(this, REQUEST_CODE, i, 0);
        long firstTime = SystemClock.elapsedRealtime();
        AlarmManager am = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME, firstTime, sender);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkUser();
    }

    public void onSaveTaskClick(View view)
    {
        if (task == null)
        {
            task = new Task();
            task.setIsNew(true);
        }

        // Reset errors.
        mTaskNameView.setError(null);

        String taskName = mTaskNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid taskName
        if (TextUtils.isEmpty(taskName))
        {
            mTaskNameView.setError(getString(R.string.error_invalid_task_name));
            focusView = mTaskNameView;
            cancel = true;
        }

        if (cancel)
        {
            focusView.requestFocus();
        } else
        {
            task.setName(taskName);

            RadioButton selectedPriority = (RadioButton) findViewById(mPriorityView.getCheckedRadioButtonId());
            task.setPriority(Enum.valueOf(Task.Priority.class, selectedPriority.getText().toString().toUpperCase()));

            RadioButton selectedCategory = (RadioButton) findViewById(mCategoryView.getCheckedRadioButtonId());
            task.setCategory(Enum.valueOf(Task.Category.class, selectedCategory.getText().toString().toUpperCase()));

            if (mAssigneeView.getSelectedItem() == null)
            {
                return;
            }
            task.setAssignee((String) mAssigneeView.getSelectedItem());

            if (mLocationView.getSelectedItem() == null)
            {
                return;
            }
            task.setLocation((String) mLocationView.getSelectedItem());

            task.setDueDate(dueDate.getTime().getTime());

            task.setTeamName(ParseUser.getCurrentUser().getString("team"));

            task.setAcceptStatus(Task.AcceptStatus.WAITING);
            task.setStatus(Task.Status.WAITING);

            TasksProvider provider = new TasksProvider(this);
            provider.insert(task);

            provider.close();
            //sendNewTaskNotification(task.getId(),task.getTeamName(),task.getName());

            finish();
        }
    }
}
