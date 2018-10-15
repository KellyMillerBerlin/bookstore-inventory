package com.example.android.bookstoreinventory;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
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
 * Created by Kelly Miller on 30.07.2018.
 * Activity for user to add a new book to inventory.
 */

public class InputActivity extends AppCompatActivity {

    // Content URI for new book
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

    // Starting quantity
    int quantity = 0;

    // Boolean to detect edits/inputs (false being no edits made)
    private Boolean mEditsMade = false;

    // Listens for a view to be touched, signaling that edits have been made
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

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(mPriceEditText.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(mSupplierEditText.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(mContactEditText.getWindowToken(), 0);

    }

    /**
     * Reduces quantity but prevents not to a negative number
     */
    public void incrementQuantity(View view) {
        if (quantity < 15) {
            quantity = quantity + 1;
            displayQuantity(quantity);
        } else {
            Toast.makeText(this, getString(R.string.quantity_controls),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Increases quantity but only to 15
     */
    public void decrementQuantity(View view) {
        if (quantity > 0) {
            quantity = quantity - 1;
            displayQuantity(quantity);
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
        // Inflate the menu options from the res/menu/menu_catalog.xml file and add to
        // input_activity view
        getMenuInflater().inflate(R.menu.menu_input, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveBook();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                if (!mEditsMade) {
                    NavUtils.navigateUpFromSameTask(InputActivity.this);
                    return true;
                }
                // If edits have been made, notify the user to save changes
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicks "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(InputActivity.this);
                            }
                        };
                // Save changes?
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveBook() {

        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String contactString = mContactEditText.getText().toString().trim();
        String quantityString = mQuantityText.getText().toString();

        // Check if user has entered anything
        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(contactString)
                && quantityString.equals("0") ) {
            return;
        }

        // Create a ContentValues object where column names are the keys and entries the values
        ContentValues values = new ContentValues();
        values.put(BooksEntry.COLUMN_PRODUCT, nameString);
        values.put(BooksEntry.COLUMN_QUANTITY, quantityString);
        values.put(BooksEntry.COLUMN_PRICE, priceString);
        values.put(BooksEntry.COLUMN_SUPPLIER, supplierString);
        values.put(BooksEntry.COLUMN_PHONE, contactString);

        // Insert new book, returning URI, as long as there are no null values
        if (!nameString.isEmpty() && !quantityString.equals("0") && !priceString.isEmpty()
                && !supplierString.isEmpty() && !contactString.isEmpty()) {
            Uri newUri = getContentResolver().insert(BooksEntry.CONTENT_URI, values);
            // Insertion unsuccessful, notify user
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_failed),
                        Toast.LENGTH_SHORT).show();
            }
            // Insertion successful, notify user
            Toast.makeText(this, getString(R.string.insert_successful),
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this,
                    getString(R.string.missing_values),
                    Toast.LENGTH_LONG).show();
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
                        // User has clicked "Discard" button, close the current activity
                        finish();
                    }
                };
        // Show dialog alerting user that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Warning to user about changes lost if trying to exit without saving
     *
     * @param discardButtonClickListener click listener for user confirmation
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Creates an alert dialogue and configures the message options
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard_changes, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_making_changes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User has clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
