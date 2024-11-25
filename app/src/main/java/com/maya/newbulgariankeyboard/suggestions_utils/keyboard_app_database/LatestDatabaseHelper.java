package com.maya.newbulgariankeyboard.suggestions_utils.keyboard_app_database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper;
import com.maya.newbulgariankeyboard.main_utils.LatestUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class LatestDatabaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH;
    private static String DB_NAME;
    private SQLiteDatabase database;
    public final Context context;
    public String TAG = "LatestDatabaseHelper";
    public LatestPreferencesHelper prefs;

    LatestDatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
        this.context = context;
        DB_PATH = LatestUtils.Companion.getDbFilePathAndroid11(context);
        DB_NAME = databaseName;
        prefs = LatestPreferencesHelper.Companion.getDefaultInstance(context);
        prefs.initAppPreferences();
        prefs.sync();
        if (prefs.getMThemingApp().isDbInstalled()) {
            Log.d(TAG, "Db installed preferences are true");
            openDataBase();
        } else {
            Log.d(TAG, "Db installed preferences are false");
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    private void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            Log.d(TAG, "Database Not exists");
            /*new*/
            // this.getReadableDatabase();
            // new DownloadFileFromURL().execute(LatestUtils.Companion.getDB_FILE_PATH());
            //copyDataBase(); /*old*/
        } else {
            Log.i(TAG, "Database already exists");
        }
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDb = null;
        try {
            String path = DB_PATH + DB_NAME;
            checkDb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            Log.e(TAG, "Error while checking db");
            return false;
        }
        if (checkDb != null) {
            checkDb.close();
        }
        return checkDb != null;
    }

    private void copyDataBase() throws IOException {
        Log.d(TAG, " copyDataBase Db");
        InputStream externalDbStream = context.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream localDbStream = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = externalDbStream.read(buffer)) > 0) {
            Log.d(TAG, " Copying: ");
            localDbStream.write(buffer, 0, bytesRead);
        }

        localDbStream.close();
        externalDbStream.close();
    }

    SQLiteDatabase openDataBase() throws SQLException {
        String path = DB_PATH + DB_NAME;
        if (database == null) {
            createDataBase();
            try {
                database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "Not Creating Db");
        }
        return database;
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}