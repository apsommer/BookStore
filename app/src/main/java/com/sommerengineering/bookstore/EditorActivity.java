package com.sommerengineering.bookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sommerengineering.bookstore.data.BookContract.BookEntry;

import java.util.Locale;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // fields to capture user input
    private EditText mNameEditText;
    private EditText mAuthorEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;
    private Button mIncreaseButton;
    private Button mDecreaseButton;
    private ImageButton mCallButton;

    // integer ID of cursor loader
    private static final int BOOK_LOADER = 0;

    // content URI for the selected existing book, null if new book
    private Uri mSelectedBookURI;

    // flag for unsaved user changes when navigating away from activity
    private boolean mBookHasChanged;

    // touch listener is set on a view, a touch implies the field has changed
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // defer to super class constructor for initialization
        super.onCreate(savedInstanceState);

        // set layout to activity_editor
        setContentView(R.layout.activity_editor);

        // get the intent which started this activity, always from catalog activity
        Intent intent = getIntent();

        // extract the URI included with the intent
        mSelectedBookURI = intent.getData();

        // if the URI is null, the FAB button was pressed and the activity is in "insert mode"
        if (mSelectedBookURI == null) {

            // update app bar title
            setTitle(R.string.editor_activity_title_add_a_book);

            // since a book is being added, the option to delete should be hidden from the menu
            // this invalidation causes the system to call onPrepareOptionsMenu()
            invalidateOptionsMenu();

        // else the URI exists, and the activity is in "edit mode" for an existing single book
        } else {

            // update app bar title
            setTitle(R.string.editor_activity_title_edit_book);

            // initialize loader
            getLoaderManager().initLoader(BOOK_LOADER, null, this);
        }

        // get references to all relevant views for user input
        mNameEditText = (EditText) findViewById(R.id.edit_book_name);
        mAuthorEditText = (EditText) findViewById(R.id.edit_book_author);
        mPriceEditText = (EditText) findViewById(R.id.edit_book_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_book_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_book_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_book_supplier_phone);
        mDecreaseButton = (Button) findViewById(R.id.decrease_button);
        mIncreaseButton = (Button) findViewById(R.id.increase_button);
        mCallButton = (ImageButton) findViewById(R.id.call_button);

        // set a listener on each user input field
        mNameEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);

        // set a listener on the quantity "+" button
        mIncreaseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // get value from edittext field
                String quantityString = mQuantityEditText.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);

                quantity += 1;

                // increment the edittext value by one
                mQuantityEditText.setText(String.format(Locale.getDefault(), "%d", quantity));

            }
        });

        // set a listener on the quantity "+" button
        mDecreaseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // get value from edittext field
                String quantityString = mQuantityEditText.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);

                // zero is the lowest quantity allowed
                if (quantity ==0) {
                    return;
                }
                quantity -= 1;

                // decrement the edittext value by one
                mQuantityEditText.setText(String.format(Locale.getDefault(), "%d", quantity));

            }
        });

        // set a listener on the supplier call button
        mCallButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // get value from edittext field
                String phoneNumber = mSupplierPhoneEditText.getText().toString().trim();

                // do nothing if the supplier phone number is blank
                if (TextUtils.isEmpty(phoneNumber)) {
                    return;
                }

                // create implicit intent to dial a phone number
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phoneNumber));

                // ensure an app capable of handling the dial intent exists on the phone
                if (dialIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(dialIntent);
                }

            }
        });

    }

    // called by the system when in "insert book" mode to hide the delete option from the overflow menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // defer to super class for initialization
        super.onPrepareOptionsMenu(menu);

        // hide the option to delete
        if (mSelectedBookURI == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    // create and show the "unsaved changes" dialog box
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        // alert dialog builder constructs the attributes of the message box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // primary message title
        builder.setMessage(R.string.unsaved_changes_dialog_msg);

        // positive button means to ignore the unsaved changes and continue with navigation
        builder.setPositiveButton(R.string.discard_changes, discardButtonClickListener);

        // negative button means cancel the navigation attempt and stay in the editor activity
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {

            // return back to editor activity
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

    // create and show the "delete confirmation" dialog box
    private void showDeleteConfirmationDialog() {

        // alert dialog builder constructs the attributes of the message box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // primary message title
        builder.setMessage(R.string.dialog_msg_delete_single_book);

        // positive button is a confirmation to delete the book from the database
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            // call helper method to perform the delete
            @Override
            public void onClick(DialogInterface dialog, int i) {
                deleteBook();
            }
        });

        // negative button means cancel the navigation attempt and stay in the editor activity
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            // return back to editor activity
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
    private void deleteBook() {

        // if the URI exists, then the activity is in "edit mode" for an existing single book
        // if the URI is null, the FAB button was pressed and the activity is in "insert mode"
        if (mSelectedBookURI != null) {

            // perform a delete on the provider using a content resolver
            int deletedRows = getContentResolver().delete(mSelectedBookURI, null, null);

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

        // exit activity and return to catalog activity
        finish();

    }

    // if the user presses the back button with unsaved changes a warning dialog box appears
    @Override
    public void onBackPressed() {

        // if the user has not entered anything then proceed with normal back button behavior
        if (!mBookHasChanged) {
            super.onBackPressed();
        }

        // define a click listener for the "discard changes" button
        // this ends the editor activity
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                finish();
            }
        };

        // pass the discard button click listener to the alert dialog helper method
        showUnsavedChangesDialog(discardButtonClickListener);

    }

    // save user inputs to persistent database
    private void saveBook() {

        // get raw state of user fields as strings
        String name = mNameEditText.getText().toString().trim();
        String author = mAuthorEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierPhone = mSupplierPhoneEditText.getText().toString().trim();

        // if all fields are blank assume the user made a mistake and exit without saving
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(author) && TextUtils.isEmpty(priceString)
            && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierName)
                && TextUtils.isEmpty(supplierPhone)) {
            return;
        }

        // book title field must be entered to insert new book
        if (TextUtils.isEmpty(name)) {

            String toastMessage = getString(R.string.toast_enter_name);

            // display toast message
            Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
            toast.show();

            return;
        }

        // default values book attributes, used if user leaves field blank
        double price = 0;
        int quantity = 0;



        // under normal conditions the user specifies a price
        if (!TextUtils.isEmpty(quantityString)) {
            price = Double.parseDouble(priceString);
        }

        // under normal conditions the user specifies a quantity
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        // container for key : value pairs
        ContentValues values = new ContentValues();

        // add key : value pairs for each feature
        values.put(BookEntry.COLUMN_BOOK_NAME, name);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, author);
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE, supplierPhone);

        // toast to display success (or failure) of save action
        String toastMessage;

        // if the URI is null, the FAB button was pressed and the activity is in "insert mode"
        if (mSelectedBookURI == null) {

            // perform an insert on the provider using a content resolver
            Uri newBookURI = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // row insert failed and therefore returned insert URI is null
            if (newBookURI == null) {
                toastMessage = getString(R.string.toast_save_failed);

            // row insert successful
            } else {
                toastMessage = getString(R.string.toast_save_successful);
            }

        // if the URI exists, then the activity is in "edit mode" for an existing single book
        } else {

            // perform an update using the provider through a content resolver
            int updatedRow = getContentResolver().update(mSelectedBookURI, values, null, null);

            // row insert failed and therefore the number of affected rows is zero
            if (updatedRow == 0) {
                toastMessage = getString(R.string.toast_save_failed);

            // row insert successful
            } else {
                toastMessage = getString(R.string.toast_save_successful);
            }

        }

        // display toast message
        Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
        toast.show();

    }

    // overflow menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // add options menu to app bar by inflating menu_editor
        getMenuInflater().inflate(R.menu.menu_editor_activity, menu);
        return true;
    }

    // click listener for items in the options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // get id of selected item
        switch (item.getItemId()) {

            // menu option "Save"
            case R.id.action_save:

                // helper method to save existing book data in sqlite database
                saveBook();

                // exit activity and return to catalog activity
                finish();
                return true;

            // menu option "Delete"
            case R.id.action_delete:

                // ask the user for confirmation using a dialog box
                showDeleteConfirmationDialog();
                return true;

            // this is the up arrow button in top left of app bar
            // the first part of the signature "android." means this is a framework component
            case android.R.id.home:

                // if the user has not entered anything then proceed with normal up button behavior
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // define a click listener for the "discard changes" button
                // this ends the editor activity
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                // pass the discard button click listener to the alert dialog helper method
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }

        // defer to super class for correct data type to return
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // define projection (column names) for query
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_AUTHOR, BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY, BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE};

        // CursorLoader requires that the column projection includes the _ID column
        return new CursorLoader(this, mSelectedBookURI, projection, null, null, null);

    }

    // the cursor is only a single row representing a single book
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // exit method if finished cursor is null or empty
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // move cursor to the only row which is first position
        if (cursor.moveToFirst()) {

            // get column indices
            int nameIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int authorIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
            int priceIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierNameIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int supplierPhoneIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE);

            // get each value at each column index
            String name = cursor.getString(nameIndex);
            String author = cursor.getString(authorIndex);
            float price = cursor.getFloat(priceIndex);
            int quantity = cursor.getInt(quantityIndex);
            String supplierName = cursor.getString(supplierNameIndex);
            String supplierPhone= cursor.getString(supplierPhoneIndex);

            // set the proper values in each user input field
            mNameEditText.setText(name);
            mAuthorEditText.setText(author);
            mPriceEditText.setText(String.format(Locale.getDefault(), "%.2f", price));
            mQuantityEditText.setText(String.format(Locale.getDefault(), "%d", quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhone);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // clear all selections
        mNameEditText.getText().clear();
        mAuthorEditText.getText().clear();
        mPriceEditText.getText().clear();
        mQuantityEditText.getText().clear();
        mSupplierNameEditText.getText().clear();
        mSupplierPhoneEditText.getText().clear();

    }

}
