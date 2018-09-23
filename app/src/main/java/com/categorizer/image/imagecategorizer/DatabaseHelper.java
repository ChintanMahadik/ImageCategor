package com.categorizer.image.imagecategorizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME1 = "IMAGE_TAGS";
    private static final String TABLE_NAME2 = "LAST_TAGGED";
    private static final String COL1 = "ID";
    private static final String COL2 = "TAG";


    public DatabaseHelper(Context context,String TABLE_NAME) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable1 = "CREATE TABLE " + TABLE_NAME1 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 +" TEXT)";
        String createTable2 = "CREATE TABLE " + TABLE_NAME2 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, FOLDER_NAME TEXT, LAST_COUNT INTEGER)";
        db.execSQL(createTable1);
        db.execSQL(createTable2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME1);
        onCreate(db);
    }

    public boolean addData(String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item);

        Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME1);

        long result = db.insert(TABLE_NAME1, COL2, contentValues);
        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean SAVE_LAST_TAGGED(String folder_name,int index) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("FOLDER_NAME", folder_name);
        contentValues.put("LAST_COUNT", index);
        System.out.println("Folder Name is "+folder_name);
        String query="SELECT * FROM LAST_TAGGED WHERE FOLDER_NAME = '"+folder_name+"'";
        Cursor data = db.rawQuery(query, null);
        System.out.println(data);
        Cursor d = null;
        long result = 0;
        if(data.getCount()>0) {
            System.out.println("Update");
            //d = db.rawQuery("update " + TABLE_NAME2 + " set LAST_COUNT="+index+" where FOLDER_NAME='" + folder_name + "'",null);
            db.execSQL("UPDATE LAST_TAGGED SET LAST_COUNT='"+index+"' WHERE FOLDER_NAME='"+folder_name+"'");
        }
        else{
            System.out.println("New Insert");
            result = db.insert(TABLE_NAME2, COL2, contentValues);
        }
        //if date as inserted incorrectly it will return -1
        if (result == -1 ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns all the data from database
     * @return
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT Distinct "+COL2+" FROM " + TABLE_NAME1;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getLASTTAGGED(String folder_name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT LAST_COUNT,FOLDER_NAME from LAST_TAGGED where FOLDER_NAME='"+folder_name+"'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }
}
