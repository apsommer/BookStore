package com.sommerengineering.bookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.security.Provider;
import com.sommerengineering.bookstore.data.BookContract.BookEntry;

// content provider
public class BookProvider extends ContentProvider {

    // tag for log messages
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    // database helper object
    private BookDbHelper mDbHelper;

    // codes for URI matcher
    private static final int BOOKS = 100; // entire table
    private static final int BOOK_ID = 101; // specific row

    // UriMatcher object matches a content URI to an integer code
    // the input passed to the constructor the integer code to return if the root URI is passed
    // it is common to use the framework constant NO_MATCH for this default case
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // static initializer is executed the first time anything is called from this class
    static {

        // associate the format of the URI address to an integer code
        // for example, the first line assigns code 100 to "content://com.sommerengineering.bookstore/books"
        mUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        mUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);

    }

    // initialize provider and database helper
    @Override
    public boolean onCreate() {

        // create and initialize a database helper object
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    // perform a SQL query on a given URI
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // get reference to readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // cursor holds the result of the query, equivalent to a SQL window
        Cursor cursor;

        // get format match code for the URI
        final int match = mUriMatcher.match(uri);

        switch (match) {

            // full books table
            case BOOKS:

                // perform a query on the entire books table
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            // specific row in books table
            case BOOK_ID:

                // the ? and array pattern protects against SQL injection hacker attacks
                // number of ? in selection must match number of elements in selectionArgs[]
                // equivalent to string "_id=?"
                selection = BookEntry._ID + "=?";

                // parseId extracts only the integer id from the content URI
                // creates an string array holding a single element with "#", where # is any integer
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // perform a query on the books table where _id equals #, resulting in a single row cursor
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }

        // set a notification for this content URI
        // if the data at this URI changes, this cursor becomes invalid
        // and query() method is automatically run again to refresh it
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    // insert new data into provider
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        // get pattern match code for URI
        final int match = mUriMatcher.match(uri);

        switch (match) {

            // full books table
            case BOOKS:

                // helper method returns content URI for this new row
                return insertBook(uri, contentValues);

            // case BOOK_ID: this case will never happen as insert is always with respect to the end of the entire table

            // only the full table case is matched, everything else throws exception
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }
    }

    // insert book into database with the given attributes
    // return content URI address for this new row
    private Uri insertBook(Uri uri, ContentValues values) {

        // check validity of name value
        // extract the value from the key : value pair
        String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Book requires a name!");
        }

        // check validity of the book price
        // use a capital letter Float rather than float since we are checking for nullity
        Float price = values.getAsFloat(BookEntry.COLUMN_BOOK_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Book requires a valid price!");
        }

        // check validity of quantity value
        // use a capital Integer rather than int since we are checking for nullity
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if (quantity != null && quantity < 0) { // null is acceptable as the sqlite database will default to 0
            throw new IllegalArgumentException("Book requires a valid quantity!");
        }

        // the author name, supplier name, and supplier phone can all be null, no need to check

        // get reference to writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // insert new row into the books table and get the new row id
        long newRowId = database.insert(BookEntry.TABLE_NAME, null, values);

        // if the insertion failed then newRowId = -1, return null
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Insertion failed for: " + uri);
            return null;
        }

        // if 1 or more rows of were inserted then notify all listeners to this URI
        // any cursor pointing to this URI is invalidated when the change notification occurs
        // and the system calls the provider query() to refresh that cursor
        if (newRowId > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the new URI with the new row ID appended to it
        return ContentUris.withAppendedId(uri, newRowId);
    }

    // update data at the given selection
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        // get format match code for URI
        final int match = mUriMatcher.match(uri);

        // notify all listeners to this URI
        // any cursor pointing to this URI is invalidated when the change notification occurs
        // and the system calls the provider query() to refresh that cursor
        getContext().getContentResolver().notifyChange(uri, null);

        switch (match) {

            // full books table
            case BOOKS:

                // helper method returns integer number of rows updated
                return updateBook(uri, contentValues, selection, selectionArgs);

            // specific row in books table
            case BOOK_ID:

                // the ? and array pattern protects against SQL injection hacker attacks
                // number of ? in selection must match number of elements in selectionArgs[]
                // equivalent to string "_id=?"
                selection = BookEntry._ID + "=?";

                // parseId extracts only the integer id from the content URI
                // creates an string array holding a single element with "#", where # is any integer
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // helper method returns integer for number of rows updated
                return updateBook(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update failed for: " + uri);

        }

    }

    // update book in database with given content values, return integer for number of rows updated
    // an update is an edit of existing data and can affect any number of table values
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // check validity of name value, if it exists
        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {

            // extract the value from the key : value pair
            String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Book requires a name!");
            }
        }

        // check validity of price value, if it exists
        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {

            // use a capital letter Float rather than float since we are checking for nullity
            Float price = values.getAsFloat(BookEntry.COLUMN_BOOK_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Book requires a valid price!");
            }
        }

        // check validity of quality value, if it exists
        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {

            // use a capital Integer rather than int since we are checking for nullity
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (quantity != null && quantity < 0) { // null is acceptable as the sqlite database will default to 0
                throw new IllegalArgumentException("Book requires a valid quantity!");
            }
        }

        // the author name, supplier name, and supplier phone can all be null, no need to check

        // a final check that there is actually something to update
        if (values.size() == 0) {
            return 0;
        }

        // get reference to writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // update row(s) in books table, and get the number of total rows affected
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        // if 1 or more rows of have changed then notify all listeners to this URI
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return total number of rows updated
        return rowsUpdated;

    }

    // delete data at the given selection
    // return integer number of rows deleted
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // get reference to writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // get format match code for URI
        final int match = mUriMatcher.match(uri);

        // track the number of rows deleted
        int rowsDeleted;

        switch (match) {

            // full books table
            case BOOKS:

                // delete all rows at the selection and selection arguments
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;

            // specific row in books table
            case BOOK_ID:

                // the ? and array pattern protects against SQL injection hacker attacks
                // number of ? in selection must match number of elements in selectionArgs[]
                // equivalent to string "_id=?"
                selection = BookEntry._ID + "=?";

                // parseId extracts only the integer id from the content URI
                // creates an string array holding a single element with "#", where # is any integer
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // delete a single row given by the ID in the URI
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for: " + uri);

        }

        // if 1 or more rows of were deleted then notify all listeners to this URI
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;

    }

    // get MIMI data type stored in a content URI
    @Override
    public String getType(Uri uri) {

        // get pattern match code for URI
        final int match = mUriMatcher.match(uri);

        switch (match) {

            // full books table
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;

            // specific row in books table
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri + " with match = " + match);
        }

    }

}
