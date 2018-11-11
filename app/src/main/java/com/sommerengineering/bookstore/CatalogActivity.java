package com.sommerengineering.bookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sommerengineering.bookstore.data.BookContract;
import com.sommerengineering.bookstore.data.BookDbHelper;
import com.sommerengineering.bookstore.data.BookContract.BookEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // integer ID of cursor loader
    private static final int BOOK_LOADER = 0;

    // reference to cursor adapter that populates list view in activity_catalog
    BookCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // defer to superclass constructor for initialization
        super.onCreate(savedInstanceState);

        // set the view as activity_catalog
        setContentView(R.layout.activity_catalog);

        // setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // explicit intent to open editor activity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);

            }

        });

        // initialize loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);

        // get reference to list view in activity_catalog
        ListView listView = (ListView) findViewById(R.id.list_view);

        // get view reference for empty state and set it on the list view
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        // create new cursor adapter and set it on the list view
        // the cursor input parameter is null because a loader supplies cursors to the adapter
        mAdapter = new BookCursorAdapter(this, null);
        listView.setAdapter(mAdapter);

        // setup click listener for items in the list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // clicking an item in the list view opens the editor activity in "edit mode" for that book
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // explicit intent to open editor activity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // include the content URI for the selected pet with the intent
                Uri selectedBookURI = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                intent.setData(selectedBookURI);

                // start editor activity in "edit mode"
                startActivity(intent);

            }

        });

    }

    // create an overflow menu in the app bar, defaults to top right
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate the menu options from layout menu_catalog_activity
        getMenuInflater().inflate(R.menu.menu_catalog_activity, menu);
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
                return true;

            // option: delete all entries
            case R.id.action_delete_all_entries:

                showDeleteConfirmationDialog();
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

        // perform an insert on the provider using a content resolver
        // the correct content URI is defined as a constant in BookContract
        getContentResolver().insert(BookEntry.CONTENT_URI, values);

    }

    // create and show the "delete confirmation" dialog box
    private void showDeleteConfirmationDialog() {

        // alert dialog builder constructs the attributes of the message box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // primary message title
        builder.setMessage(R.string.dialog_msg_delete_all_books);

        // positive button is a confirmation to delete all books from the database
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            // call helper method to perform the delete
            @Override
            public void onClick(DialogInterface dialog, int i) {
                deleteAllBooks();
            }
        });

        // negative button means cancel the navigation attempt and stay in the editor activity
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            // return back to activity
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create and show the constructed dialog box
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    // helper method called when delete button is pressed in the delete confirmation dialog
    private void deleteAllBooks() {

        // perform a delete on the provider using a content resolver
        int deletedRows = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);

        // toast to display success (or failure) of delete action
        String toastMessage;

        // row delete failed and therefore the number of deleted rows is zero
        if (deletedRows == 0) {
            toastMessage = getString(R.string.toast_delete_failed);

        // row delete successful
        } else {
            toastMessage = getString(R.string.toast_delete_successful);
        }

        // display toast message
        Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
        toast.show();

    }

    // called by initLoader() in onCreate()
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // define projection (column names) for query
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_BOOK_NAME, BookEntry.COLUMN_BOOK_PRICE, BookEntry.COLUMN_BOOK_QUANTITY};

        // CursorLoader requires the column projection includes the _ID column
        return new CursorLoader(this, BookEntry.CONTENT_URI, projection, null, null, null);

    }

    // called by the system when a new cursor is finished being created by the loader
    // refresh the adapter with this new cursor
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    // called by the system when a previously created loader is being reset
    // therefore the cursor data is no longer valid and the adapter is cleared
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

//    // helper method TODO more comments
//    public void decreaseQuantity(int rowID, int quantity) {
//
//        // TODO more comments
//        quantity -= 1;
//
//        // TODO more comments
//        ContentValues values = new ContentValues();
//        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
//
//        // TODO comments
//        Uri updateURI = ContentUris.withAppendedId(BookEntry.CONTENT_URI, rowID);
//
//        // TODO comments
//        getContentResolver().update(updateURI, values, null, null);
//
//    }

}
