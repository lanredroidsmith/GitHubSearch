package com.lanredroidsmith.githubsearch.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;
import com.lanredroidsmith.githubsearch.ui.user.OnUsersReadFromFileListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Lanre on 11/6/17.
 */

public class MainUtils {
    private static final String TAG = MainUtils.class.getSimpleName();

    public static final String BASE_URL = "https://api.github.com/search/";
    public static final String ACCEPT_HEADER = "Accept: application/vnd.github.v3+json";
    public static final String USER_SEARCH_URL = "url";
    public static final String TOTAL_ITEMS = "ti";
    public static final String FIRST_TOTAL = "ft";
    public static final String NEXT_URL = "nu";
    public static final String FAVORITE_USERS_FOLDER_NAME = "Favorite Users";

    private static final String CACHED_USERS_FILE = "cachedusers.dat";
    public static final String APP_NAME = "GitHub Search";

    // keys
    public static final String TOTAL_ITEMS_KEY = "tik";
    public static final String CURRENT_TOTAL_KEY = "ctk";
    public static final String NEXT_URL_KEY = "nuk";
    public static final String IS_LOADING_KEY = "ilk";
    public static final String CURRENT_POSITION_KEY = "cpk";

    public static String getNextUrl(String linkHeader) {
        String nextEndpoint;
        if (null == linkHeader) { return null; }
        linkHeader = linkHeader.trim();
        try {
            HashMap<String, String> linkHeaderMap = new HashMap<>();
            if (!TextUtils.isEmpty(linkHeader))
            {
                String[] linkHeaderArray = linkHeader.split(",");
                for (int i = 0; i < linkHeaderArray.length; i++)
                {
                    //must split into 2, considering a sample link header from GitHub, else there's a problem
                    String[] innerArray = linkHeaderArray[i].split(";");
                    if (innerArray.length != 2)
                    {
                        throw new Exception("Inner Link Header Array Strange Split Count");
                    }
                    //remove the angular brackets from both ends of the url, remove any whitespace first
                    innerArray[0] = innerArray[0].trim().substring(1, innerArray[0].length() - 1);
                    //split the 'rel' part, trim it first
                    String[] relVal = innerArray[1].trim().split("=");
                    if (relVal.length != 2 )
                    {
                        //it's in the form e.g. rel="next", so if it doesn't split into 2, then there's a problem
                        throw new Exception("Link Header Rel Value Strange Split Count");
                    }
                    //remove the double quotes from both ends, remove any whitespace first
                    innerArray[1] = relVal[1].trim().substring(1, relVal[1].length() - 1);

                    //remove any whitespace from the key too
                    linkHeaderMap.put(innerArray[1].trim(), innerArray[0]);
                }

                if (linkHeaderMap.containsKey("next"))
                {
                    nextEndpoint = linkHeaderMap.get("next");
                }
                else
                {
                    nextEndpoint = null;
                }
            } else {
                nextEndpoint = null;
            }
        } catch (Exception e) {
            nextEndpoint = null;
        }
        return nextEndpoint;
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public static void getUsersFromFile(WeakReference<Context> context,
                                        final WeakReference<OnUsersReadFromFileListener> listener,
                                        final WeakReference<Bundle> bundle) {
        try {
            final Context c = context.get();
            // IO operations should be done off the main thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (c != null) {
                        try {
                            FileInputStream fis = c.openFileInput(CACHED_USERS_FILE);
                            ObjectInputStream ois = new
                                    ObjectInputStream(fis);
                            if (listener.get() != null) {
                                listener.get().onUsersRead((ArrayList<GitHubUser>) ois.readObject(),
                                        bundle);
                            }
                            ois.close(); // this is closed 1st
                            fis.close();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void writeUsersToFile(WeakReference<Context> context,
                                        final ArrayList<GitHubUser> users) {
        try {
            final Context c = context.get();
            // IO operations should be done off the main thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (c != null) {
                        try {
                            FileOutputStream fos = c.openFileOutput(CACHED_USERS_FILE,
                                    Context.MODE_PRIVATE);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(users);
                            oos.close(); // this is closed 1st
                            fos.close();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void deleteAvatar(Context mContext, String avatar) {
        try {
            String storageState = Environment.getExternalStorageState();
            if (Environment.MEDIA_SHARED.equals(storageState)) {
                Toast.makeText(mContext, mContext.getString(R.string.could_not_del_avatar_storage_shared)
                        , Toast.LENGTH_SHORT).show();
            } else if (Environment.MEDIA_MOUNTED.equals(storageState)) {
                String favoriteUsersDir = Environment.getExternalStorageDirectory() + File.separator +
                        mContext.getString(R.string.app_name) + File.separator + FAVORITE_USERS_FOLDER_NAME;
                File avatarFile = new File(favoriteUsersDir, avatar);
                if (avatarFile.exists())
                {
                    if (!avatarFile.delete())
                    {
                        Toast.makeText(mContext
                                , mContext.getString(R.string.unable_to_del_avatar)
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.could_not_del_avatar_storage_not_found)
                        , Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}