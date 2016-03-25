package com.liron.ots;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.liron.ots.db.TasksProvider;
import com.liron.ots.users.LoginActivity;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SaveMemberTaskActivity extends AppCompatActivity
{
    ParseUser memberCurrentUser;
    TextView mCategoryReplyView;
    TextView mTimeReplyView;
    TextView mLocationReplyView;
    TextView mPriorityReplyView;
    RadioGroup memberAcceptView;
    RadioGroup memberStatusView;
    LinearLayout taskStatusLayout;
    LinearLayout addPhotoLayout;
    ImageView taskImage;
    Task task;
    Bitmap photo;

    private static final int CAMERA_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_member_task);

        initViews();
        checkUser();

        String taskId = getIntent().getStringExtra("task_id");
        TasksProvider provider = new TasksProvider(this);
        try
        {
            task = provider.query(taskId);
        }
        finally
        {
            provider.close();
        }

        if (task == null)
        {
            finish();
        }
        else
        {
            String photo = task.getPhoto();
            setViewsValues(task);
        }
    }

    private void initViews()
    {
        mCategoryReplyView = (TextView) findViewById(R.id.m_category);
        mTimeReplyView = (TextView) findViewById(R.id.m_duetime);
        mLocationReplyView = (TextView) findViewById(R.id.m_location);
        mPriorityReplyView = (TextView) findViewById(R.id.m_priority);
        memberAcceptView = (RadioGroup) findViewById(R.id.status);
        memberStatusView = (RadioGroup) findViewById(R.id.taskStatus);
        taskStatusLayout = (LinearLayout) findViewById(R.id.taskStatusLayout);
        addPhotoLayout = (LinearLayout) findViewById(R.id.addPhotoLayout);
        taskImage = (ImageView) findViewById(R.id.taskImage);

        taskStatusLayout.setVisibility(View.GONE);
        addPhotoLayout.setVisibility(View.GONE);

        memberAcceptView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                RadioButton selectedAcceptStatus = (RadioButton) findViewById(checkedId);
                switch (Enum.valueOf(Task.AcceptStatus.class, selectedAcceptStatus.getText().toString().toUpperCase()))
                {
                    case ACCEPT:
                        taskStatusLayout.setVisibility(View.VISIBLE);
                        break;
                    default:
                        taskStatusLayout.setVisibility(View.GONE);
                        break;
                }
            }
        });

        memberStatusView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                RadioButton selectedTaskStatus = (RadioButton) findViewById(checkedId);
                switch (Enum.valueOf(Task.Status.class, selectedTaskStatus.getText().toString().replace(" ", "_").toUpperCase()))
                {
                    case DONE:
                        addPhotoLayout.setVisibility(View.VISIBLE);
                        break;
                    default:
                        addPhotoLayout.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    private void setViewsValues(Task task)
    {
        mCategoryReplyView.setText(task.getCategory().name());
        mPriorityReplyView.setText(task.getPriority().name());
        mLocationReplyView.setText(task.getCategory().name());
        SimpleDateFormat spf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        mTimeReplyView.setText(spf.format(new Date(task.getDueDate())));

        ArrayList<View> btns = new ArrayList<>();
        memberAcceptView.findViewsWithText(btns, task.getAcceptStatus().name(), View.FIND_VIEWS_WITH_TEXT);
        ((RadioButton)btns.get(0)).setChecked(true);

        btns.clear();
        memberStatusView.findViewsWithText(btns, task.getStatus().name().replace("_", " "), View.FIND_VIEWS_WITH_TEXT);
        ((RadioButton)btns.get(0)).setChecked(true);

        new DownloadImageTask(taskImage).execute(task.getPhoto());
    }


    private boolean checkUser()
    {
        memberCurrentUser = ParseUser.getCurrentUser();
        if (memberCurrentUser == null)
        {
            // show the signup or login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!checkUser())
        {
            return;
        }
    }

    public void onAddPhotoClick(View view)
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            taskImage.setImageBitmap(photo);
            taskImage.setVisibility(View.VISIBLE);
        }
    }

    public void onSaveMemberTaskClick(View view)
    {
        RadioButton selectedAcceptStatus = (RadioButton) findViewById(memberAcceptView.getCheckedRadioButtonId());
        task.setAcceptStatus(Enum.valueOf(Task.AcceptStatus.class, selectedAcceptStatus.getText().toString().toUpperCase()));

        RadioButton selectedStatus = (RadioButton) findViewById(memberStatusView.getCheckedRadioButtonId());
        task.setStatus(Enum.valueOf(Task.Status.class, selectedStatus.getText().toString().replace(" ", "_").toUpperCase()));

        if (photo != null)
        {
            //TODO: save photo file
            Bitmap bmp = photo;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            final ParseFile photoFile = new ParseFile(task.getName() + "." + task.getAssignee() + ".png", byteArray);

            photoFile.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    TasksProvider provider = new TasksProvider(SaveMemberTaskActivity.this);

                    task.setPhoto(photoFile.getUrl());
                    provider.update(task);

                    provider.close();
                }
            });
        }
        else
        {
            TasksProvider provider = new TasksProvider(this);
            provider.update(task);

            provider.close();
        }

        finish();
    }
}




