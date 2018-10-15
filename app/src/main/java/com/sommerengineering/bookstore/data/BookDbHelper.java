package com.sommerengineering.bookstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// contract inner class
import com.sommerengineering.bookstore.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    // established convention that database version begins at 1
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    // defer to superclass constructor for initialization
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // create new persistent sqlite database on device
    @Override
    public void onCreate(SQLiteDatabase db) {

        // SQL create statement
        // CREATE TABLE books (_id INTEGER, name TEXT, breed TEXT, gender INTEGER, weight INTEGER);
        String SQL_CREATE_PETS_TABLE =
                "CREATE TABLE " + BookEntry.TABLE_NAME + " (" +
                        BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, " +
                        BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL, " +
                        BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                        BookEntry.COLUMN_BOOK_SUPPLIER_NAME + " TEXT, " +
                        BookEntry.COLUMN_BOOK_SUPPLIER_PHONE + " TEXT );";

        db.execSQL(SQL_CREATE_PETS_TABLE);

    }

    // update existing sqlite database on device
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
