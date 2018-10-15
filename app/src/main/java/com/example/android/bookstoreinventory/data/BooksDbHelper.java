package com.example.android.bookstoreinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.bookstoreinventory.data.BooksContract.BooksEntry;

/**
 * Created by Kelly Miller on 24.07.2018.
 */

public class BooksDbHelper extends SQLiteOpenHelper {

    // Log tag
    public static final String LOG_TAG = BooksDbHelper.class.getSimpleName();
    // Name of the database file
    public static final String DATABASE_NAME = "books.db";
    // Database version. If changed, must increment.
    public static final int DATABASE_VERSION = 1;

    // Public constructor with @param context
    public BooksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create String that includes SQL statement to create a new table
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BooksEntry.TABLE_NAME
                + " (" + BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BooksEntry.COLUMN_PRODUCT + " TEXT NOT NULL, "
                + BooksEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BooksEntry.COLUMN_PRICE + " TEXT NOT NULL, "
                + BooksEntry.COLUMN_SUPPLIER + " TEXT, "
                + BooksEntry.COLUMN_PHONE + " TEXT);";

        // Execute SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
        Log.i(LOG_TAG, "Table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
