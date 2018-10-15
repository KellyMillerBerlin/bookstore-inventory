package com.example.android.bookstoreinventory;

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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.bookstoreinventory.data.BooksContract.BooksEntry;

/**
 * Created by Kelly Miller on 01.08.2018.
 */

public class ViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for book loader
    private static final int EXISTING_BOOKS_LOADER = 0;

    // Content URI for current book
    private Uri mCurrentBookUri;

    private TextView mProductField;
    private TextView mQuantityField;
    private TextView mSupplierField;
    private TextView mContactField;
    private TextView mPriceField;

    private boolean mQuantityChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_activity);

        // Setup FAB to open EditActivity to edit a book in the inventory
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewActivity.this, EditorActivity.class);
                intent.setData(mCurrentBookUri);
                startActivity(intent);
            }
        });


        // Get Uri
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // Start up loader
        getLoaderManager().initLoader(EXISTING_BOOKS_LOADER, null, this);
    }

    /**
     * Connects user to phone service to call supplier
     */
    public void callSupplier(View view) {
        mContactField = (TextView) findViewById(R.id.view_contact_String);
        String phoneNumber = mContactField.getText().toString();
        if (!TextUtils.isEmpty(phoneNumber)) {
            Uri call = Uri.fromParts("tel", phoneNumber, null);
            Intent intent = new Intent(Intent.ACTION_DIAL, call);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, getString(R.string.no_number),
                    Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Reduces quantity but prevents a negative number
     */
    public void incrementQuantity(View view) {
        String quantityString = mQuantityField.getText().toString();
        int quantity = Integer.parseInt(quantityString);
        if (quantity < 15) {
            quantity = quantity + 1;
            displayQuantity(quantity);
            mQuantityChanged = true;
        } else {
            Toast.makeText(this, getString(R.string.quantity_controls),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Increases quantity but only to 15
     */
    public void decrementQuantity(View view) {
        String quantityString = mQuantityField.getText().toString();
        int quantity = Integer.parseInt(quantityString);
        if (quantity > 0) {
            quantity = quantity - 1;
            displayQuantity(quantity);
            mQuantityChanged = true;
        } else {
            Toast.makeText(this, getString(R.string.quantity_controls),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void displayQuantity(int quantity) {
        mQuantityField.setText(String.valueOf(quantity));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file and add to input_activity view
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_save:
                saveQuantity();
                return true;
            case R.id.home:
            case android.R.id.home:
                if (!mQuantityChanged) {
                    NavUtils.navigateUpFromSameTask(ViewActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(ViewActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Prompt the user to confirm that they want to delete this entry after menu selection
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User has clicked the "Delete" button, so delete the book
                deleteEntry();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Creates an alert dialogue and configures the message options
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard_quantity, discardButtonClickListener);
        builder.setNegativeButton(R.string.save_quantity, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    saveQuantity();
                    finish();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Deletes entry being viewed and closes activity
     */
    private void deleteEntry() {

        int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null,
                null);

        // Notify user if delete was successful
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete
            Toast.makeText(this, getString(R.string.view_delete_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful, so display a toast
            Toast.makeText(this, getString(R.string.view_delete_successful),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void saveQuantity() {

        // Read from fields displayed
        String nameString = mProductField.getText().toString();
        String priceString = mPriceField.getText().toString();
        String supplierString = mSupplierField.getText().toString();
        String contactString = mContactField.getText().toString();
        String quantityString = mQuantityField.getText().toString();

        // Check if quantity has been changed before updating data
        if (!mQuantityChanged) {
            return;
        }

        // Create a ContentValues object where column names are the keys and entries the values
        ContentValues values = new ContentValues();
        values.put(BooksEntry.COLUMN_PRODUCT, nameString);
        values.put(BooksEntry.COLUMN_QUANTITY, quantityString);
        values.put(BooksEntry.COLUMN_PRICE, priceString);
        values.put(BooksEntry.COLUMN_SUPPLIER, supplierString);
        values.put(BooksEntry.COLUMN_PHONE, contactString);

        // Update fields for book
        int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null,
                null);

        // Notify user if update was successful
        if (rowsAffected == 0) {
            Toast.makeText(this, getString(R.string.edit_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.edit_successful),
                    Toast.LENGTH_SHORT).show();
        }

        finish();

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
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
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

            mProductField = (TextView) findViewById(R.id.view_product_String);
            mQuantityField = (TextView) findViewById(R.id.view_quantity_String);
            mPriceField = (TextView) findViewById(R.id.view_price_String);
            mSupplierField = (TextView) findViewById(R.id.view_supplier_String);
            mContactField = (TextView) findViewById(R.id.view_contact_String);

            // Update the views on the screen with the values from the database
            mProductField.setText(product);
            mQuantityField.setText(quantityString);
            mPriceField.setText(price);
            mSupplierField.setText(supplier);
            mContactField.setText(contact);
        }
    }

    @Override
    public void onLoaderReset (Loader < Cursor > loader) {
    }

}
