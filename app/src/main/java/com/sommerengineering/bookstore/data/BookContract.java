package com.sommerengineering.bookstore.data;

import android.provider.BaseColumns;

// "final" access modifier used because this class only contains constants
public final class BookContract {

    // each inner class is an individual sqlite table
    public static final class BookEntry implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "books";

        // int _ID is inherent to Android framework
        public static final String _ID = BaseColumns._ID;

        // String name is the book name
        public static final String COLUMN_BOOK_NAME = "name";

        // String author is the author name
        public static final String COLUMN_BOOK_AUTHOR = "author";

        // float price is the book price in USD
        public static final String COLUMN_BOOK_PRICE = "price";

        // int quantity is the total number of this item in stock
        public static final String COLUMN_BOOK_QUANTITY = "quantity";

        // String supplier_name is the supplier name
        public static final String COLUMN_BOOK_SUPPLIER_NAME = "supplier_name";

        // String supplier_phone is the supplier phone number
        public static final String COLUMN_BOOK_SUPPLIER_PHONE = "supplier_phone";

    }
}
