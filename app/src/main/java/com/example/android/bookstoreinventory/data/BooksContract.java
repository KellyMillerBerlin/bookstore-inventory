package com.example.android.bookstoreinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Kelly Miller on 24.07.2018.
 */

public final class BooksContract {

    // Private constructor, no need for outside use
    private BooksContract() {}

    // Content authority constant to build custom URI
    public static final String CONTENT_AUTHORITY = "com.example.android.bookstoreinventory";

    // Blueprint for URI plus parsing
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Base path
    public static final String PATH_BOOKS = "books";

    public static final class BooksEntry implements BaseColumns {

        // Content URI for content provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // MIME type to access a list of books
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // MIME type to access a single book (inventory item)
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // Table name
        public static final String TABLE_NAME = "books";

        /** Column names
         * with Type
         */
        // _id, type INTEGER
        public final static String _ID = BaseColumns._ID;
        // Book title, type TEXT
        public static final String COLUMN_PRODUCT = "product_name";
        // Price in usd, type TEXT
        public static final String COLUMN_PRICE = "price";
        // Quantity, type INTEGER
        public static final String COLUMN_QUANTITY = "quantity";
        // Supplier, type TEXT
        public static final String COLUMN_SUPPLIER = "supplier_name";
        // Phone number of supplier, type TEXT
        public static final String COLUMN_PHONE = "contact";

    }
}
