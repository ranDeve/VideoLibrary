package com.example.ran.videolibraryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySqlHelper extends SQLiteOpenHelper {

    public MySqlHelper(Context context) {
        super(context, "VideoLibrary.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE MyVideostable (_id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, body TEXT, movieurl TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
