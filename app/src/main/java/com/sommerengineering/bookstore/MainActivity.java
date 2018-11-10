package com.sommerengineering.bookstore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.sommerengineering.bookstore.data.BookDbHelper;
import com.sommerengineering.bookstore.data.BookContract.BookEntry;

public class MainActivity extends AppCompatActivity {

    // subclass of SQLiteOpenHelper manages interactions with sqlite databases
    private BookDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // initialize activity with super constructor and set layout View
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // temporary helper method displays database contents to MainActivity
        displayDatabase();

    }

    @Override
    protected void onStart() {

        // defer to super constructor
        super.onStart();

        // temporary helper method displays database contents to MainActivity
        displayDatabase();
    }

    // create an overflow menu in the app bar, defaults to top right
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate the menu options from layout menu_main_activity
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    // create options within the menu
    // behaves as a listener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // get item that user clicked
        switch (item.getItemId()) {

            // option: insert placeholder data
            case R.id.action_insert_placeholder_data:

                insertPlaceholderBook();
                displayDatabase();
                return true;

            // option: delete all entries
            case R.id.action_delete_all_entries:

                // TODO delete all rows of table
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // insert placeholder data for a single book as a new row in sqlite table
    private void insertPlaceholderBook() {

        // container for key : value pairs
        ContentValues values = new ContentValues();

        // add key : value pairs for each feature
        // BookEntry._ID is autoincremented
        values.put(BookEntry.COLUMN_BOOK_NAME, "The Spot");
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, "David Means");
        values.put(BookEntry.COLUMN_BOOK_PRICE, 7.99);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, 12);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, "Penguin Publishers");
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE, "800-455-8234");

        // value assigned to mDbHelper in onCreate
        // get reference to sqlite database inventory.db in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // returns row position for successful insert and -1 for error
        db.insert(BookEntry.TABLE_NAME, null, values);

    }

    // display sqlite database values
    private void displayDatabase() {

        // assign value to helper that manages interactions with sqlite databases
        mDbHelper = new BookDbHelper(this);

        // get reference to sqlite database inventory.db in read mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // define projection (column names) for query
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_AUTHOR, BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY, BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE};

        // query the books table
        // equivalent to SQL query "SELECT * FROM books;"
        Cursor cursor = db.query(BookEntry.TABLE_NAME, projection,
                null, null, null, null, null, null);

        // get reference to textview in layout activity_catalog
        TextView displayView = (TextView) findViewById(R.id.textview_activity_main);

        // try-finally block ensures that the cursor is always closed
        try {

            // display the number of rows in the Cursor (= number of rows in pets table)
            displayView.setText("The books table contains " + cursor.getCount() + " books.\n\n");
            displayView.append(BookEntry._ID + " - " + BookEntry.COLUMN_BOOK_NAME +
                    " - " + BookEntry.COLUMN_BOOK_AUTHOR + " - " + BookEntry.COLUMN_BOOK_PRICE +
                    " - " + BookEntry.COLUMN_BOOK_QUANTITY + " - " + BookEntry.COLUMN_BOOK_SUPPLIER_NAME +
                    " - " + BookEntry.COLUMN_BOOK_SUPPLIER_PHONE + "\n");

            // get index position for each column
            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME );
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE);

            // iterate through all rows, cursor starts at row -1 (column titles)
            // therefore, first moveToNext() puts Cursor at row 0
            while (cursor.moveToNext()) {

                // use indices to extract table values
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentAuthor = cursor.getString(authorColumnIndex);
                float currentPrice = cursor.getFloat(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentSupplierPhone = cursor.getString(supplierPhoneColumnIndex);

                displayView.append("\n" + currentID + " - " + currentName + " - " + currentAuthor
                        + " - " + currentPrice + " - " + currentQuantity + " - " + currentSupplierName
                        + " - " + currentSupplierPhone);

            }

        // always close cursor to prevent memory leaks
        } finally {
            cursor.close();
        }

    }

}
