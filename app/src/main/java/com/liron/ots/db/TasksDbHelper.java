package com.liron.ots.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
    Creates the database if doesn't exists
 */
public class TasksDbHelper extends SQLiteOpenHelper
{

    private static final int DB_VERSION = 11;

    private static final String DB_NAME = "otsdbtasks.db";

    public TasksDbHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sql =
                "CREATE TABLE " + TasksDbConsts.TABLE_NAME
                        + "("
                        + TasksDbConsts._ID + " TEXT PRIMARY KEY,"
                        + TasksDbConsts.NAME + " TEXT ,"
                        + TasksDbConsts.TEAM_NAME + " TEXT ,"
                        + TasksDbConsts.ASSIGNEE + " TEXT ,"
                        + TasksDbConsts.LOCATION + " TEXT ,"
                        + TasksDbConsts.CATEGORY + " TEXT ,"
                        + TasksDbConsts.PRIORITY + " TEXT ,"
                        + TasksDbConsts.ACCEPT_STATUS + " TEXT ,"
                        + TasksDbConsts.STATUS + " TEXT ,"
                        + TasksDbConsts.PHOTO_URL + " TEXT ,"
                        + TasksDbConsts.DUE_DATE + " INTEGER"

                        /*
                        + TasksCons.LAT + " REAL ,"
                        + TasksCons.LNG + " REAL "*/
                        + ")";
        db.execSQL(sql);


//        String CreateUserTable = "CREATE TABLE " + TasksDbConsts.TABLE_USER +
//                "(" + TasksDbConsts.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + TasksDbConsts.COLUMN_USER_NAME + " TEXT,"
//                + TasksDbConsts.COLUMN_USER_PASSWORD + " TEXT,"
//                + TasksDbConsts.COLUMN_USER_EMAIL + " TEXT,"
//                + TasksDbConsts.COLUMN_USER_PHONE + " TEXT,"
//                + TasksDbConsts.COLUMN_USER_REGISTER+ " INTEGER);";
//        db.execSQL(CreateUserTable);


    }

    public void rebuild(SQLiteDatabase db)
    {
        String sql = "DROP TABLE IF EXISTS " + TasksDbConsts.TABLE_NAME;
        db.execSQL(sql);

        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        rebuild(db);
    }
}


