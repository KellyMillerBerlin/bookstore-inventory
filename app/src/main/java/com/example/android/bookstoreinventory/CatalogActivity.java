package com.example.android.bookstoreinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.example.android.bookstoreinventory.data.BooksContract.BooksEntry;


public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the book data loader */
    private static final int BOOKS_LOADER = 0;

    /** Adapter for the ListView */
    BookCursorAdapter mCursorAdapter;

    // Log tag
    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_activity);

        // Setup FAB to open InputActivity to add a new book to the inventory
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView bookListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Set up an Adapter to create a list item for each row of book data in the Cursor
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        // Kick off the loader
        getLoaderManager().initLoader(BOOKS_LOADER, null, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file and add to
        // input_activity view
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        item.getItemId();
        insertBook();
        Log.i(LOG_TAG, "Book inserted");
        return super.onOptionsItemSelected(item);
    }

    // Inserts dummy data
    private void insertBook() {

        // Create ContentValues object with desired entry information passed to table
        ContentValues values = new ContentValues();
        values.put(BooksEntry.COLUMN_PRODUCT, "There There");
        values.put(BooksEntry.COLUMN_QUANTITY, "1");
        values.put(BooksEntry.COLUMN_PRICE, "$12.99");
        values.put(BooksEntry.COLUMN_SUPPLIER, "Powells Books");
        values.put(BooksEntry.COLUMN_PHONE, "503-324-6792");

        // Insert a new row using the ContentResolver (data gatekeeper) and retrieve new URI.
        Uri newUri = getContentResolver().insert(BooksEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Define a projection (String array) with desired columns for query
        String[] projection = {
                BooksEntry._ID,
                BooksEntry.COLUMN_PRODUCT,
                BooksEntry.COLUMN_PRICE,
                BooksEntry.COLUMN_QUANTITY,
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                BooksEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
