package com.example.android.bookstoreinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.bookstoreinventory.data.BooksContract.BooksEntry;

/**
 * Created by Kelly Miller on 04.08.2018.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    // Identifier for book loader
    private static final int EXISTING_BOOKS_LOADER = 0;

    // Content URI for book
    private Uri mCurrentBookUri;

    // Edit text product name, i.e. book title
    private EditText mNameEditText;

    // Edit text for price
    private EditText mPriceEditText;

    // Text field for quantity
    private TextView mQuantityText;

    // Edit text for supplier
    private EditText mSupplierEditText;

    // Edit text for supplier phone number
    private EditText mContactEditText;

    // Boolean to detect edits/inputs (false being no edits made)
    private Boolean mEditsMade = false;

    // Starting quantity
    private int quantity = 0;

    /**
     * OnTouchListener that listens for touches on a view (user editing)
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mEditsMade = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity);

        // Get Uri
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // Initialize a loader to read the pet data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_BOOKS_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier);
        mContactEditText = (EditText) findViewById(R.id.edit_contact);
        mQuantityText = (TextView) findViewById(R.id.text_quantity);

        // Determine if user has edited fields (need to save or not)
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mContactEditText.setOnTouchListener(mTouchListener);

        mContactEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(mPriceEditText.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(mSupplierEditText.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(mContactEditText.getWindowToken(), 0);

    }

    /**
     * Increases quantity but only to 15
     */
    public void incrementQuantity(View view) {
        String quantityString = mQuantityText.getText().toString();
        int quantity = Integer.parseInt(quantityString);
        if (quantity < 15) {
            quantity = quantity + 1;
            displayQuantity(quantity);
            mEditsMade = true;
        } else {
            Toast.makeText(this, getString(R.string.quantity_controls),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Decreases quantity but only to 0
     */
    public void decrementQuantity(View view) {
        String quantityString = mQuantityText.getText().toString();
        int quantity = Integer.parseInt(quantityString);
        if (quantity > 0) {
            quantity = quantity - 1;
            displayQuantity(quantity);
            mEditsMade = true;
        } else {
            Toast.makeText(this, getString(R.string.quantity_controls),
                    Toast.LENGTH_SHORT).show();
        }
    }


    public void displayQuantity(int quantity) {
        mQuantityText.setText(String.valueOf(quantity));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file and add to input_activity view
        getMenuInflater().inflate(R.menu.menu_input, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveChanges();
                return true;
            case android.R.id.home:
                if (!mEditsMade) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void saveChanges() {

        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String contactString = mContactEditText.getText().toString().trim();
        String quantityString = mQuantityText.getText().toString();

        // Check if all fields are blank before sending data
        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(contactString) && mQuantityText.equals("0") ) {
            return;
        }

        // Create a ContentValues object where column names are the keys and entries the values.
        ContentValues values = new ContentValues();
        values.put(BooksEntry.COLUMN_PRODUCT, nameString);
        values.put(BooksEntry.COLUMN_QUANTITY, quantityString);
        values.put(BooksEntry.COLUMN_PRICE, priceString);
        values.put(BooksEntry.COLUMN_SUPPLIER, supplierString);
        values.put(BooksEntry.COLUMN_PHONE, contactString);

    // Update book if no null values.
        if (!nameString.isEmpty() && !priceString.isEmpty() && !supplierString.isEmpty() && !contactString.isEmpty()) {
            // Update fields for book, returning rows affected
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            // Notify user if update was successful.
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.edit_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.edit_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
                Toast.makeText(this, getString(R.string.missing_values_editor), Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onBackPressed() {
        if (!mEditsMade) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Warning to save changes before exiting activity
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Creates an alert dialogue and configures the message options
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard_changes, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_making_changes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Projection that contains all columns in the table
        String[] projection = {
                BooksEntry._ID,
                BooksEntry.COLUMN_PRODUCT,
                BooksEntry.COLUMN_QUANTITY,
                BooksEntry.COLUMN_PRICE,
                BooksEntry.COLUMN_SUPPLIER,
                BooksEntry.COLUMN_PHONE};

        // Executes query on background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Only proceed if more than one row
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find columns of attributes to display
            int productColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_PRODUCT);
            int quantityColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_SUPPLIER);
            int contactColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_PHONE);

            // Extract out the value from the Cursor for the given column index
            String product = cursor.getString(productColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String quantityString = Integer.toString(quantity);
            String price = cursor.getString(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String contact = cursor.getString(contactColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(product);
            mQuantityText.setText(quantityString);
            mPriceEditText.setText(price);
            mSupplierEditText.setText(supplier);
            mContactEditText.setText(contact);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
