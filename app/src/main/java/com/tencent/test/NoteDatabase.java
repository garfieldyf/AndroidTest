package com.tencent.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.ext.util.DebugUtils;
import android.util.Log;

public class NoteDatabase extends SQLiteOpenHelper {
    public NoteDatabase(Context context) {
        super(context, "notes.db", null, 1);
    }

    public long insert(long offset) {
        final SQLiteDatabase db = getWritableDatabase();
        Log.i("yf", DebugUtils.toString(db, new StringBuilder("db = ")).toString());
        final ContentValues values = new ContentValues();
        values.put("_date", System.currentTimeMillis() + offset);
        values.put("service", "service_" + offset);
        values.put("func", "func_" + offset);
        values.put("_data", "data_" + offset);
        return db.insert("notes", null, values);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS notes (" +
                   "_date INTEGER PRIMARY KEY," +
                   "service TEXT," +
                   "func TEXT," +
                   "_data TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
