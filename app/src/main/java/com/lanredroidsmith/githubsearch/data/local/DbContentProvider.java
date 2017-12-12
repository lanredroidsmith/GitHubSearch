package com.lanredroidsmith.githubsearch.data.local;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.lanredroidsmith.githubsearch.data.local.DbContract.FavoriteUsers.TABLE_NAME;

public class DbContentProvider extends ContentProvider {
    public DbContentProvider() {
    }

    private DbHelper mDbHelper;

    // Define final integer constants for the directory of favorite users and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    private static final int FAVORITE_USERS = 100;
    private static final int FAVORITE_USER_WITH_ID = 101;
    private static final int FAVORITE_USER_WITH_LOGIN = 102;

    // We declare a static variable for the Uri matcher that we construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /* Define a static buildUriMatcher method that associates URI's with their int match
     * Initialize a new matcher object without any matches,
     * then use .addURI(String authority, String path, int match) to add matches
     */
    private static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the favorite users directory and a single item by ID.
         */
        uriMatcher.addURI(DbContract.AUTHORITY, DbContract.PATH_FAVORITE_USERS, FAVORITE_USERS);
        uriMatcher.addURI(DbContract.AUTHORITY, DbContract.PATH_FAVORITE_USERS + "/#",
                FAVORITE_USER_WITH_ID);
        uriMatcher.addURI(DbContract.AUTHORITY, DbContract.PATH_FAVORITE_USERS + "/*",
                FAVORITE_USER_WITH_LOGIN);

        return uriMatcher;
    }

    /*
    * onCreate() is where you should initialize anything you'll need to setup
    * your underlying data source.
    * In this case, we're working with a SQLite database, so we'll need to
    * initialize a DbHelper to gain access to it.
    * */
    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted tasks
        int rowsDeleted; // starts as 0

        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case FAVORITE_USER_WITH_ID:
                // Use selections/selectionArgs to filter for this ID
                rowsDeleted = db.delete(TABLE_NAME, DbContract.FavoriteUsers._ID + "=?",
                        new String[]{ uri.getPathSegments().get(1) });
                break;
            case FAVORITE_USER_WITH_LOGIN:
                rowsDeleted = db.delete(TABLE_NAME, DbContract.FavoriteUsers.COLUMN_LOGIN + "=?",
                        new String[]{ uri.getPathSegments().get(1) });
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (rowsDeleted != 0) {
            // A favorite user was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Get access to the database (to write new data to)
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case FAVORITE_USERS:
                // Insert new values into the database
                // Inserting values into favorite_users table
                long id = db.insert(TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(DbContract.FavoriteUsers.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor cursor;

        // Query for the tasks directory and write a default case
        switch (match) {
            // Query for the tasks directory
            case FAVORITE_USERS:
                cursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVORITE_USER_WITH_LOGIN:
                cursor =  db.query(TABLE_NAME,
                        projection,
                        DbContract.FavoriteUsers.COLUMN_LOGIN + "=?",
                        new String[]{ uri.getPathSegments().get(1) },
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
