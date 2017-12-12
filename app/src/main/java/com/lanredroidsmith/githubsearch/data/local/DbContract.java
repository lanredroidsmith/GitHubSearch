package com.lanredroidsmith.githubsearch.data.local;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Lanre on 11/13/17.
 */

public final class DbContract {

    // The authority, which is how our code knows which Content Provider to access
    public static final String AUTHORITY = "com.lanredroidsmith.githubsearch";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "favorite_users" directory
    public static final String PATH_FAVORITE_USERS = FavoriteUsers.TABLE_NAME;

    // To prevent someone from accidentally instantiating th contract class,
    // make the constructor private
    private DbContract() {}

    /* FavoriteUsers table and contents */
    public static final class FavoriteUsers implements BaseColumns {

        // content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE_USERS).build();

        public static final String TABLE_NAME = "favorite_users";
        public static final String COLUMN_LOGIN = "login";
        public static final String COLUMN_AVATAR = "avatar";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_HTML_URL = "html_url";
    }
}
