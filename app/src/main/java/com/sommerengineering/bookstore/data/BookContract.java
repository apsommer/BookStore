package com.sommerengineering.bookstore.data;

import android.provider.BaseColumns;

// "final" access modifier used because this class only contains constants
public final class BookContract {

    // each inner class is an individual sqlite table
    public static final class BookEntry implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "books";

        // column names
        public static final String _ID = BaseColumns._ID; // _ID is inherent to Android framework
        public static final String COLUMN_BOOK_NAME = "name";
        public static final String COLUMN_BOOK_PRICE = "price";
        public static final String COLUMN_BOOK_QUANTITY = "quantity";
        public static final String COLUMN_BOOK_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_BOOK_SUPPLIER_PHONE = "supplier_phone";

    }
}
