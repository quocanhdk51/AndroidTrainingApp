package com.example.s3727634.afinal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "history";
    //table must include: date, score/levels completed, steps taken, no. of locations
    private static final String COL1 = "date";
    private static final String COL2 = "words";
    private static final String COL3 = "steps";
    private static final String COL4 = "locations";

    public DatabaseHelper(Context context){
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL1 + " TEXT PRIMARY KEY, " +
                COL2 + " TEXT, "+
                COL3 + " TEXT, "+
                COL4 + " TEXT)";
        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public boolean addData(String date, String words, String steps, String loc){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, date);
        contentValues.put(COL2, words);
        contentValues.put(COL3, steps);
        contentValues.put(COL4, loc);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1) return false;
        else return true;
    }

    public boolean addData(){
        SQLiteDatabase db = this.getWritableDatabase();

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String d = df.format(c);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, d);
        contentValues.put(COL2, 0);
        contentValues.put(COL3, 0);
        contentValues.put(COL4, 0);
        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1) return false;
        else return true;
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public boolean checkDate() {
        SQLiteDatabase db = this.getWritableDatabase();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String d = df.format(c);
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME + " WHERE " + COL1 + "='" + d + "'";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            addData(d, "0", "0", "0");
//            Log.d(TAG, "abcEMPTY, add new");
            return false;
        }
        cursor.close();
        return true;
    }

    public int getStep(){
        SQLiteDatabase db = this.getWritableDatabase();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String d = df.format(c);
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + "='" + d + "'";
        Cursor row = db.rawQuery(query, null);
        if(row.getCount() < 1) {
            addData();
            row = db.rawQuery(query, null);
        }
        row.moveToPosition(0);
//        Log.d(TAG, "no of columns " + row.getColumnCount());
//        Log.d(TAG, "current position " + row.getPosition());
//        Log.d(TAG, "no of rows " + row.getCount());
//        Log.d(TAG, "column 2 = " + row.getColumnName(2));
        int steps = Integer.parseInt(row.getString(2));
        row.close();
        return steps;
    }

    public void setStep(String newStep){
        SQLiteDatabase db = this.getWritableDatabase();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String d = df.format(c);
        String query = "UPDATE " + TABLE_NAME + " SET " + COL3 + "= '" + newStep +
                "' WHERE " + COL1 + " = '" + d + "'";
        db.execSQL(query);
    }

    public int getWords(){
        SQLiteDatabase db = this.getWritableDatabase();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String d = df.format(c);
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + "='" + d + "'";
        Cursor row = db.rawQuery(query, null);
        if(row.getCount() < 1) {
            addData();
            row = db.rawQuery(query, null);
        }
        row.moveToPosition(0);
        int words = Integer.parseInt(row.getString(1));
        row.close();
        return words;
    }

    public void setWords(String newWords){
        SQLiteDatabase db = this.getWritableDatabase();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String d = df.format(c);
        String query = "UPDATE " + TABLE_NAME + " SET " + COL2 + "= '" + newWords +
                "' WHERE " + COL1 + " = '" + d + "'";
        db.execSQL(query);
    }

    public int getLocs(){
        SQLiteDatabase db = this.getWritableDatabase();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String d = df.format(c);
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + "='" + d + "'";
        Cursor row = db.rawQuery(query, null);
        if(row.getCount() < 1) {
            addData();
            row = db.rawQuery(query, null);
        }
        row.moveToPosition(0);
        int words = Integer.parseInt(row.getString(3));
        row.close();
        return words;
    }

    public void setLocs(String newWords){
        SQLiteDatabase db = this.getWritableDatabase();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String d = df.format(c);
        String query = "UPDATE " + TABLE_NAME + " SET " + COL4 + "= '" + newWords +
                "' WHERE " + COL1 + " = '" + d + "'";
        db.execSQL(query);
    }
}

