package com.example.android.bookstoreinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreinventory.data.BooksContract;

/**
 * Created by Kelly Miller on 30.07.2018.
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */

public class BookCursorAdapter extends CursorAdapter {

    private Context mContext;

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views to populate with data from the table
        TextView nameTextView = (TextView) view.findViewById(R.id.name_catalog);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_catalog);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_catalog);
        Button saleButton = (Button) view.findViewById(R.id.sale);
        Button detailsButton = (Button) view.findViewById(R.id.details);

        // Find the columns of specific book attributes
        int nameColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_PRODUCT);
        int quantityColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_PRICE);

        // Read the data from the Cursor for the current book
        String productName = cursor.getString(nameColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);
        final int quantity = Integer.parseInt(productQuantity);
        String productPrice = cursor.getString(priceColumnIndex);

        // Update the TextViews with the attributes for the current book
        nameTextView.setText(productName);
        quantityTextView.setText(productQuantity);
        priceTextView.setText(productPrice);
        final int id = cursor.getInt(cursor.getColumnIndex(BooksContract.BooksEntry._ID));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext = context;
                if (quantity > 0) {
                    int newQuantity = quantity - 1;

                    // Getting the URI with the append of the ID for the row
                    Uri quantityUri = ContentUris.withAppendedId(BooksContract.BooksEntry.CONTENT_URI, id);
                    // Update the value
                    ContentValues values = new ContentValues();
                    values.put(BooksContract.BooksEntry.COLUMN_QUANTITY, newQuantity);
                    mContext.getContentResolver().update(quantityUri, values, null, null);
                } else Toast.makeText(mContext, R.string.out_of_stock, Toast.LENGTH_LONG).show();
            }
        });

        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mContext = context;
                // Create new intent to open the ViewActivity for the specific book
                Intent intent = new Intent(mContext, ViewActivity.class);
                // Getting the URI with the append of the ID for the row
                Uri currentBookUri = ContentUris.withAppendedId(BooksContract.BooksEntry.CONTENT_URI, id);
                intent.setData(currentBookUri);
                mContext.startActivity(intent);

            }
        });

    }
}
