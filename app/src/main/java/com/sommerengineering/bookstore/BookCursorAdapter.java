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
        TextView authorTextView = (TextView) view.findViewById(R.id.author);

        // get index position for each column
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);

        // extract data from cursor
        String nameString = cursor.getString(nameColumnIndex);
        String authorString = cursor.getString(authorColumnIndex);

        // if the breed has not been specified then display a default message
        if (TextUtils.isEmpty(authorString)) {
            authorString = context.getString(R.string.unknown_author);
        }

        // set cursor data on views
        nameTextView.setText(nameString);
        authorTextView.setText(authorString);

    }
}
