package com.lanredroidsmith.githubsearch.data.local.repo;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.lanredroidsmith.githubsearch.data.local.DbContract;
import com.lanredroidsmith.githubsearch.data.local.DbContract.FavoriteUsers;
import com.lanredroidsmith.githubsearch.data.local.model.FavoriteUser;
import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;

/**
 * Created by Lanre on 11/23/17.
 */

public class UserRepository {

    private static final String TAG = UserRepository.class.getSimpleName();

    public Uri addUserToFavorites(Context context, FavoriteUser user) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(FavoriteUsers.COLUMN_LOGIN, user.getLogin());
            cv.put(FavoriteUsers.COLUMN_AVATAR, user.getAvatar());
            cv.put(FavoriteUsers.COLUMN_HTML_URL, user.getHtmlUrl());
            cv.put(FavoriteUsers.COLUMN_TYPE, user.getType());
            return context.getContentResolver().insert(FavoriteUsers.CONTENT_URI, cv);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public int removeUserFromFavorites(Context context, GitHubUser user) {
        try {
            Uri uri = FavoriteUsers.CONTENT_URI
                        .buildUpon()
                        .appendPath(user.getLogin()).build();
            return context.getContentResolver().delete(uri, null, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }

    public CursorLoader getAllFavoriteUsers(Context context) {
        try {
            return new CursorLoader(context, DbContract.FavoriteUsers.CONTENT_URI,
                    null, null, null, FavoriteUsers._ID + " ASC");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public int deleteFavoriteUser(Context context, String login) {
        try {
            Uri uri = DbContract.FavoriteUsers.CONTENT_URI;
            uri = uri.buildUpon().appendPath(login).build();
            return context.getContentResolver().delete(uri, null, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }

    public CursorLoader getUserByLogin(Context context, String login) {
        try {
            return new CursorLoader(context,
                    DbContract.FavoriteUsers.CONTENT_URI.buildUpon().appendPath(login).build(),
                    null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
}