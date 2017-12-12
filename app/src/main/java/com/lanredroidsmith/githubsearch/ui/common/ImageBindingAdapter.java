package com.lanredroidsmith.githubsearch.ui.common;

import android.databinding.BindingAdapter;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import static com.lanredroidsmith.githubsearch.util.MainUtils.APP_NAME;
import static com.lanredroidsmith.githubsearch.util.MainUtils.FAVORITE_USERS_FOLDER_NAME;

/**
 * Created by Lanre on 12/1/17.
 */

public class ImageBindingAdapter {

    private static final String TAG = ImageBindingAdapter.class.getSimpleName();

    @BindingAdapter({"imageUrl"})
    public static void loadImageFromUrl(ImageView view, String url) {
        if (TextUtils.isEmpty(url)) {
            // this is VERY necessary, else a cached view's image may be wrongly
            // used for a user to whom it doesn't belong - in the case of list items
            view.setImageBitmap(null);
            return;
        }
        // we use noFade() cos of de.hdodenhof.circleimageview.CircleImageView
        // particularly, as recommended.
        try {
            Picasso.with(view.getContext()).load(url).noFade().into(view);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @BindingAdapter({"imageFile"})
    public static void loadImageFromFile(ImageView view, String fileName) {
        if (TextUtils.isEmpty(fileName) ||
                !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // this is VERY necessary, else a cached view's image may be wrongly
            // used for a user to whom it doesn't belong - in the case of list items
            view.setImageBitmap(null);
            return;
        }

        String favoriteUsersDir = Environment.getExternalStorageDirectory() + File.separator +
                APP_NAME + File.separator + FAVORITE_USERS_FOLDER_NAME;
        File avatarFile = new File(favoriteUsersDir, fileName);
        if (!avatarFile.exists()) {
            // the user might have deleted it. For same reasons like above, we set null too
            view.setImageBitmap(null);
            return;
        }
        try {
            // we use noFade() cos of de.hdodenhof.circleimageview.CircleImageView
            // particularly, as recommended.
            Picasso.with(view.getContext()).load(avatarFile).noFade().into(view);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
