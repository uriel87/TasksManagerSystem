package com.liron.ots;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Admin on 18/12/2015.
 */
public class CheckTasksBroadCastReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        //User support library
        intent.getAction();
//        long taskId =  intent.getIntExtra("taskId", -1);
        ParseUser user = ParseUser.getCurrentUser();

        if (user == null)
        {
            return;
        }

        ParseQuery<Task> query = ParseQuery.getQuery(Task.class);
        query.whereEqualTo("team", user.getString("team"));
        query.whereEqualTo("assignee", user.getUsername());
        query.whereEqualTo("is_new", true);

        try
        {
            List<Task> tasks = query.find();

            if (tasks.size() > 0)
            {
                Intent resultIntent = new Intent(context, MainActivity.class);

                if (tasks.size() == 1)
                {
                    String taskId = tasks.get(0).getObjectId();
                    resultIntent.putExtra("task_id", taskId);
                }

                for (Task task : tasks)
                {
                    task.setIsNew(false);
                    task.save();
                }

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_info_outline_black_24dp)
                                .setAutoCancel(true)
                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_week))
                                .setContentTitle("New task - ")
                                .setContentText("Hello you have " + tasks.size() + " new tasks");

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                mBuilder.setContentIntent(resultPendingIntent);

                int mNotificationId = 001;
// Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
// Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }


        } catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
}