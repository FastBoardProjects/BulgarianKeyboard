package com.maya.newbulgariankeyboard.suggestions_utils.keyboard_app_database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.util.Log;

import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper;

import java.util.ArrayList;

public class LatestDatabaseManager {

    public   String TAG = "DatabaseManager:";
    private final Context mContext;
    private final LatestDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    public LatestPreferencesHelper prefs;

    public LatestDatabaseManager(Context context) {
        if (db != null) {
            db.close();
        }
        this.mContext = context;
        prefs = LatestPreferencesHelper.Companion.getDefaultInstance(mContext);
        prefs.initAppPreferences();
        prefs.sync();
        dbHelper = new LatestDatabaseHelper(mContext, getDatabaseName());
        if (prefs.getMThemingApp().isDbInstalled()) {
            Log.d(TAG, "Db installed preferences are true");
            db = dbHelper.openDataBase();
        } else {
            Log.d(TAG, "Db installed preferences are false");
        }
        //Log.d(TAG, "Database");
    }

    /*later use subtype logic*/
    public ArrayList<String> getAllRow(String str, String subType) {
        ArrayList<String> wordList = new ArrayList<>();
        /*     try {*/
        queryString(str, subType);
        //Log.d(TAG, "" + subType.getLocale().getDisplayName());
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            Log.d(TAG, " !cursor.isAfterLast()");
            do {
                String word = cursor.getString(0);
                word = String.valueOf(Html.fromHtml(String.valueOf(word)));
                Log.d(TAG, "Word: " + word);
                wordList.add(word);
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, " cursor.isAfterLast()");
        }
    /*    } catch (Exception e) {
            e.printStackTrace();
            Log.e("DB ERROR", e.toString());
        }*/
        return wordList;
    }

    public Integer getWordFrequency(String str, String subType) {
        String tableName = "";
        if ("en_US".equals(subType)) {
            tableName = getEnglishTableName();
        }

        Integer freq = 0;

        if (!tableName.equals("")) {

            try {

                cursor = db.rawQuery("SELECT " + getFreqColumnName() + " FROM " + tableName + " WHERE " + getWordColumnName()
                        + " = '" + str + "'", null);

                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    do {
                        freq = cursor.getInt(0);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DB ERROR", e.toString());
            }
        }
        return freq;
    }

    public Cursor queryString(String str, String subType) {
        String query;
        Log.d("MySuggestions:", " Query: " + subType);
        switch (subType) {
            case "en_US":
                query = "SELECT " + getWordColumnName() + " FROM " + getEnglishTableName() + " WHERE " + getWordColumnName()
                        + " LIKE '" + str + "%' ORDER BY " + getFreqColumnName() + " DESC LIMIT 3";

                cursor = db.rawQuery(query, null);
                break;
            //todo change
            case "bg_BG":
            /*case "bg":*/
                query = "SELECT " + getWordColumnName() + " FROM " + getBulgarianTableName() + " WHERE " + getWordColumnName()
                        + " LIKE '" + str + "%' ORDER BY " + getFreqColumnName() + " DESC LIMIT 3";
                cursor = db.rawQuery(query, null);
                break;

            default:
                query = "SELECT " + getWordColumnName() + " FROM " + getEnglishTableName() + " WHERE " + getWordColumnName()
                        + " LIKE '" + str + "%' ORDER BY " + getFreqColumnName() + " DESC LIMIT 3";

                cursor = db.rawQuery(query, null);
                break;
        }
        return cursor;
    }

    public Cursor checkWord(String str) {
        String query = "SELECT " + getWordColumnName() + " FROM " + getEnglishTableName() + " WHERE " + getWordColumnName() + " ='" + str + "'";
        try {
            cursor = db.rawQuery(query, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public void insertNewRecord(String str, String tableName) {
        Log.d("NewLfd:", " Insert new: " + str);
        String insertQuery = "INSERT INTO " + tableName
                + "(" + getFreqColumnName() + ", " + getWordColumnName() + ") VALUES ('" + 200 + "', '" + str + "' )";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(insertQuery);
        db.close();
        Log.d("NewLfd:", " Insert new Added ");
    }

    public void insertNewRecord(String str) {
        Log.d("NewLfd:", " Insert new start: " + str);
        ContentValues values = new ContentValues();
        values.put(getFreqColumnName(), 1);
        values.put(getWordColumnName(), str);
        db.insert(getEnglishTableName(), null, values);
        Log.d("NewLfd:", " Insert new close: " + str);
    }

    public void insertNewRecordWithSubtype(String str, String subtype) {
        try {
            Log.d("NewLfd:", " Insert new start: " + str + " Subtype: " + subtype);
            String currentTable = getEnglishTableName();
            switch (subtype) {
                case "en_US":
                    currentTable = getEnglishTableName();
                    break;
                //todo change
                case "bg_BG":
               /* case "bg":*/
                    currentTable = getBulgarianTableName();
                    break;
                default:
                    currentTable = getEnglishTableName();
                    break;
            }
            ContentValues values = new ContentValues();
            values.put(getFreqColumnName(), 1);
            values.put(getWordColumnName(), str);
            db.insert(currentTable, null, values);
            Log.d("NewLfd:", " Insert new close: " + str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFreq(String str) {
        Log.d("UpdateFreq", "Start");
        try {
            String insertQuery = "UPDATE " + getEnglishTableName()
                    + " SET freq = freq + 1 WHERE " + getWordColumnName()
                    + " = '" + str + "'";
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL(insertQuery);
            db.close();
            Log.d("UpdateFreq", "End");
        } catch (Exception e) {
            Log.d("UpdateFreq", e.toString());
        }
    }

    // The following methods return the database and column names from string.xml.

    private String getDatabaseName() {
        return mContext.getResources().getString(R.string.app_db_name);
    }

    /*en_US*/

    private String getEnglishTableName() {
        return mContext.getResources().getString(R.string.eng_table_name);
    }

    public String getFreqColumnName() {
        return mContext.getResources().getString(R.string.app_freq_column);
    }

    public String getWordColumnName() {
        return mContext.getResources().getString(R.string.app_word_column);
    }
    //todo change
    /*bg_BG*/
    /*bg*/
    public String getBulgarianTableName() {
        return mContext.getResources().getString(R.string.second_lang_table_name);
    }



    /**
     * Close database connection.
     */
    public void close() {
        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }
        Log.d(TAG, "Database");
    }
}