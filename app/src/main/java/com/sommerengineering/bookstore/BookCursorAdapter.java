package com.sommerengineering.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sommerengineering.bookstore.data.BookContract.BookEntry;

import java.util.Locale;

public class BookCursorAdapter extends CursorAdapter {

    // defer to super class constructor for initialization
    BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // returns a new blank view from list_item
    // the cursor is already in the correct row position
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    // sets the cursor row data on the inflated blank view from newView method
    // the cursor is already in the correct row position
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // get view references
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        final Button saleButton = (Button) view.findViewById(R.id.sale_button);

        // get index position for each column in cursor
        int rowIDIndex = cursor.getColumnIndex(BookEntry._ID);
        int nameIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        // extract data from cursor
        final int rowID = cursor.getInt(rowIDIndex);
        String name = cursor.getString(nameIndex);
        float price = cursor.getFloat(priceIndex);
        final int quantity = cursor.getInt(quantityIndex);

        // update the views with the extracted cursor data
        nameTextView.setText(name);
        priceTextView.setText(String.format(Locale.getDefault(), "$%.2f", price));
        quantityTextView.setText(String.format(Locale.getDefault(), "%d", quantity));

        // the content URI for this specific row
        final Uri currentBookURI = ContentUris.withAppendedId(BookEntry.CONTENT_URI, rowID);

        // listener for the button within each list item
        saleButton.setOnClickListener(new View.OnClickListener() {

            // reduce the quantity by 1 and update the database with this new value
            public void onClick(View v) {

                // zero is the lowest possible quantity
                if (quantity == 0) {

                    // display toast message
                    String toastMessage = context.getString(R.string.toast_quantity_at_zero);
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                // decrease quantity by 1
                int decreasedQuantity = quantity - 1;

                // update the textview to show the decreased quantity
                quantityTextView.setText(String.format(Locale.getDefault(),
                        "%d", decreasedQuantity));

                // create a container to hold the inputs for a database update
                ContentValues values = new ContentValues();

                // key : value pair for this feature (column)
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, decreasedQuantity);

                // perform an update using the provider through a content resolver
                context.getContentResolver().update(currentBookURI, values, null, null);

            }

        });

    }
}
