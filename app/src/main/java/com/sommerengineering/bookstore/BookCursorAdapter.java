package com.sommerengineering.bookstore;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.sommerengineering.bookstore.data.BookContract.BookEntry;

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
    public void bindView(View view, Context context, Cursor cursor) {

        // get references to view entities
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // get index position for each column
        int nameIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        // extract data from cursor
        String name = cursor.getString(nameIndex);
        float price = cursor.getFloat(priceIndex);
        int quantity = cursor.getInt(quantityIndex);

        // set cursor data on views
        nameTextView.setText(name);
        priceTextView.setText(String.format("$%.2f", price));
        quantityTextView.setText(Integer.toString(quantity));

    }
}
