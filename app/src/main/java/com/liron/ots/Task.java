package com.liron.ots;

import android.database.Cursor;

import com.liron.ots.db.TasksDbConsts;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.Serializable;


@ParseClassName("Task")
public class Task extends ParseObject implements Serializable
{
    private String mObjectIdCopy;
    public Task()
    {
        mObjectIdCopy = "";
    }

    public Task(Cursor cursor)
    {
        setObjectIdCopy(cursor.getString(cursor.getColumnIndex(TasksDbConsts._ID)));
        setObjectId(cursor.getString(cursor.getColumnIndex(TasksDbConsts._ID)));
        setName(cursor.getString(cursor.getColumnIndex(TasksDbConsts.NAME)));
        setAssignee(cursor.getString(cursor.getColumnIndex(TasksDbConsts.ASSIGNEE)));
        setLocation(cursor.getString(cursor.getColumnIndex(TasksDbConsts.LOCATION)));
        setDueDate(cursor.getLong(cursor.getColumnIndex(TasksDbConsts.DUE_DATE)));

        setCategory(Enum.valueOf(Category.class, cursor.getString(cursor.getColumnIndex(TasksDbConsts.CATEGORY))));
        setPriority(Enum.valueOf(Priority.class, cursor.getString(cursor.getColumnIndex(TasksDbConsts.PRIORITY))));

        setAcceptStatus(Enum.valueOf(AcceptStatus.class, cursor.getString(cursor.getColumnIndex(TasksDbConsts.ACCEPT_STATUS))));
        setStatus(Enum.valueOf(Status.class, cursor.getString(cursor.getColumnIndex(TasksDbConsts.STATUS))));

        setPhoto(cursor.getString(cursor.getColumnIndex(TasksDbConsts.PHOTO_URL)));
    }

    public void setObjectIdCopy(String objectId)
    {
        mObjectIdCopy = objectId;
    }

    public String getObjectIdCopy()
    {
        return mObjectIdCopy;
    }

    public void copyFrom(Task task)
    {
        setObjectIdCopy(task.getObjectIdCopy());
        setName(task.getName());
        setAssignee(task.getAssignee());
        setLocation(task.getLocation());
        setDueDate(task.getDueDate());

        setCategory(task.getCategory());
        setPriority(task.getPriority());

        setAcceptStatus(task.getAcceptStatus());
        setStatus(task.getStatus());

        setPhoto(task.getPhoto());
    }

    public String getPhoto() { return getString("photo_url"); }

    public void setPhoto(String photo) { if (photo != null) { put("photo_url", photo); } }

    public String getName()
{
    return getString("name");
}

    public void setName(String name)
    {
        put("name", name);
    }

    public String getTeamName()
    {
        return getString("team");
    }

    public void setTeamName(String name)
    {
        put("team", name);
    }

    public String getAssignee()
    {
        return getString("assignee");
    }

    public void setAssignee(String assignee)
    {
        put("assignee", assignee);
    }
    public String getLocation()
    {
        return getString("location");
    }

    public void setLocation(String location)
    {
        put("location", location);
    }

    public long getDueDate()
    {
        return getLong("due_date");
    }

    public void setDueDate(long dueDate)
    {
        put("due_date", dueDate);
    }

    public Category getCategory()
    {
        return Enum.valueOf(Category.class, getString("category"));
    }

    public void setCategory(Category category)
    {
        put("category", category.name());
    }

    public Priority getPriority()
    {
        return Enum.valueOf(Priority.class, getString("priority"));
    }

    public void setPriority(Priority priority)
    {
        put("priority", priority.name());
    }

    public AcceptStatus getAcceptStatus()
    {
        return Enum.valueOf(AcceptStatus.class, getString("accept_status"));
    }

    public void setAcceptStatus(AcceptStatus acceptStatus)
    {
        put("accept_status", acceptStatus.name());
    }

    public Status getStatus()
    {
        return Enum.valueOf(Status.class, getString("status"));
    }

    public void setStatus(Status status)
    {
        put("status", status.name());
    }

    public void setIsNew(boolean isNew) { put("is_new", isNew); }
    public boolean isNew() { return getBoolean("is_new"); }

    public enum Priority
    {
        NORMAL,
        URGENT,
        LOW
    }

    public enum Category
    {
        CLEANING,
        ELECTRICITY,
        COMPUTERS,
        GENERAL,
        OTHER
    }

    public enum AcceptStatus
    {
        WAITING,
        ACCEPT,
        REJECT
    }

    public enum Status
    {
        WAITING,
        IN_PROGRESS,
        DONE
    }
}
