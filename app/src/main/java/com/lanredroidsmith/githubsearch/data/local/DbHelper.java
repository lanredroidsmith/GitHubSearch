package com.lanredroidsmith.githubsearch.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lanre on 11/13/17.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "githubsearch.db";
    private static final int DB_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " +
                DbContract.FavoriteUsers.TABLE_NAME + " (" +
                DbContract.FavoriteUsers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbContract.FavoriteUsers.COLUMN_LOGIN + " TEXT NOT NULL, " +
                DbContract.FavoriteUsers.COLUMN_AVATAR + " TEXT DEFAULT NULL, " +
                DbContract.FavoriteUsers.COLUMN_TYPE + " TEXT NOT NULL, " +
                DbContract.FavoriteUsers.COLUMN_HTML_URL + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.FavoriteUsers.TABLE_NAME);
        onCreate(db);
    }
}
