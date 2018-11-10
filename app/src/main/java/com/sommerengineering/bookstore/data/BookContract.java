package com.sommerengineering.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

// final access modifier used because this class only contains constants
public final class BookContract {

    // empty constructor as no objects of this class will ever be created
    private BookContract() {}

    // content authority is the name for the entire content provider
    // a convenient string is the package name as it is guaranteed unique on the device
    public static final String CONTENT_AUTHORITY = "com.sommerengineering.bookstore";

    // portion of the content URI that is common to all tables
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // possible content URI endpoint, to be appended on common base URI
    public static final String PATH_BOOKS = "books";

    // each inner class is an individual sqlite table
    public static final class BookEntry implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "books";

        // _ID column is inherent to Android framework
        public static final String _ID = BaseColumns._ID;

        // column names
        public static final String COLUMN_BOOK_NAME = "name";
        public static final String COLUMN_BOOK_AUTHOR = "author";
        public static final String COLUMN_BOOK_PRICE = "price";
        public static final String COLUMN_BOOK_QUANTITY = "quantity";
        public static final String COLUMN_BOOK_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_BOOK_SUPPLIER_PHONE = "supplier_phone";

        // content URI for the books table
        // equivalent to "content://com.sommerengineering.bookstore/books"
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // MIME type for a list of books
        // equivalent to "vnd.android.cursor.dir/com.sommerengineering.bookstore/books"
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // MIME type for a single pet
        // equivalent to "vnd.android.cursor.item/com.sommerengineering.bookstore/books"
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

    }
}
