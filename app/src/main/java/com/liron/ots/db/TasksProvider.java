package com.liron.ots.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.liron.ots.Task;
import com.liron.ots.TasksRecyclerAdapter;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class TasksProvider
{
    private Context context;
    private TasksDbHelper helper;
    private TasksRecyclerAdapter adapter;

    public TasksProvider(Context context)
    {
        helper = new TasksDbHelper(context);
    }

    public void setAdapter(TasksRecyclerAdapter adapter)
    {
        this.adapter = adapter;
    }

    public void update(Task task)
    {
        updateParseTask(task);
    }

    public void updateDb(Task task)
    {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.updateWithOnConflict(TasksDbConsts.TABLE_NAME, taskToValues(task), TasksDbConsts._ID
                        + " =?", new String[]{task.getObjectId()},
                SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public void updateParseTask(final Task task)
    {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.class);

        query.whereEqualTo("objectId", task.getObjectIdCopy());
        query.findInBackground(new FindCallback<Task>() {
            public void done(List<Task> tasks, ParseException e) {
                if (e == null) {
                    if (tasks.size() == 0) {
                        Log.d("update_task", "no tasks found");
                        return;
                    }

                    Log.d("update_task", "Retrieved " + tasks.size() + " tasks");
                    Task foundTask = tasks.get(0);
                    foundTask.copyFrom(task);

                    updateDb(foundTask);

                    if (adapter != null) {
                        adapter.refresh();
                    }

                    foundTask.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (adapter != null) {
                                adapter.refresh();
                            }
                        }
                    });

                } else {
                    Log.d("update_task", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void deleteParseTask(final Task task)
    {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.class);
        query.whereEqualTo("objectId", task.getObjectIdCopy());
        query.findInBackground(new FindCallback<Task>()
        {
            public void done(List<Task> tasks, ParseException e)
            {
                if (e == null)
                {
                    if (tasks.size() == 0)
                    {
                        Log.d("delete_task", "no tasks found");
                        return;
                    }
                    Log.d("delete_task", "Retrieved " + tasks.size() + " tasks");

                    Task foundTask = tasks.get(0);
                    ParseObject po = ParseObject.createWithoutData("Task", foundTask.getObjectId());
                    po.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (adapter != null) {
                                adapter.refresh();
                            }
                        }
                    });
                } else
                {
                    Log.d("delete_task", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void insert(final Task task)
    {
        if (exists(task))
        {
            update(task);
        } else
        {
            task.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    task.setObjectIdCopy(task.getObjectId());
                    task.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            insertDb(task);
                            if (adapter != null)
                            {
                                adapter.refresh();
                            }
                        }
                    });
                }
            });
        }
    }

    public void insertDb(Task task)
    {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.insertWithOnConflict(TasksDbConsts.TABLE_NAME, null, taskToValues(task), SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void delete(Task task)
    {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(TasksDbConsts.TABLE_NAME, TasksDbConsts._ID + " =?",
                new String[]{task.getObjectIdCopy()});

        db.close();

        deleteParseTask(task);
    }

    public List<Task> getAll(String sortColumnName)
    {
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = queryAll(sortColumnName);
        while (cursor.moveToNext())
        {
            tasks.add(new Task(cursor));
        }
        return tasks;
    }

    public List<Task> getWaitingTasks()
    {
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = queryAll(TasksDbConsts.DUE_DATE);
        while (cursor.moveToNext())
        {
            Task task = new Task(cursor);
            if (task.getStatus() == Task.Status.WAITING)
            {
                tasks.add(task);
            }
        }
        return tasks;
    }

    public Cursor queryAll(String sortColumnName)
    {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TasksDbConsts.TABLE_NAME, null, null, null, null,
                null, sortColumnName + " COLLATE LOCALIZED ASC");
        return cursor;
    }

    public void truncate()
    {
        SQLiteDatabase db = helper.getWritableDatabase();

        try
        {
            helper.rebuild(db);
        } finally
        {
            db.close();
        }
    }

    public Task query(String objectId)
    {
        if (objectId == null)
        {
            return null;
        }

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TasksDbConsts.TABLE_NAME, null, TasksDbConsts._ID
                + " =?", new String[]{objectId}, null, null, null);

        try
        {
            if (cursor.moveToNext())
            {
                return new Task(cursor);
            }
        } finally
        {
            cursor.close();
        }

        return null;
    }

    public boolean exists(Task t)
    {
        if (t.getObjectIdCopy() == null)
        {
            return false;
        }

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(TasksDbConsts.TABLE_NAME, null, TasksDbConsts._ID
                + " =?", new String[]{t.getObjectIdCopy()}, null, null, null);

        Task task = null;

        if (cursor.moveToNext())
        {
            task = new Task(cursor);
        }

        cursor.close();

        return task != null;
    }

    public void close()
    {
        helper.close();
    }

    private ContentValues taskToValues(Task task)
    {
        ContentValues values = new ContentValues();

        values.put(TasksDbConsts._ID, task.getObjectId());
        values.put(TasksDbConsts.NAME, task.getName());
        values.put(TasksDbConsts.TEAM_NAME, task.getTeamName());
        values.put(TasksDbConsts.ASSIGNEE, task.getAssignee());
        values.put(TasksDbConsts.LOCATION, task.getLocation());
        values.put(TasksDbConsts.DUE_DATE, task.getDueDate());
        values.put(TasksDbConsts.CATEGORY, task.getCategory().name());
        values.put(TasksDbConsts.PRIORITY, task.getPriority().name());
        values.put(TasksDbConsts.ACCEPT_STATUS, task.getAcceptStatus().name());
        values.put(TasksDbConsts.STATUS, task.getStatus().name());
        values.put(TasksDbConsts.PHOTO_URL, task.getPhoto());

        return values;
    }

}
