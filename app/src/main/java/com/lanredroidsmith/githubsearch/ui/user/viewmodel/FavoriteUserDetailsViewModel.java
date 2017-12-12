package com.lanredroidsmith.githubsearch.ui.user.viewmodel;

import android.view.View;

import com.lanredroidsmith.githubsearch.data.local.model.FavoriteUser;

/**
 * Created by Lanre on 11/28/17.
 */

public class FavoriteUserDetailsViewModel {
    private FavoriteUser mUser;
    private OnUserShareListener mListener;

    public interface OnUserShareListener {
        void onUserShared(FavoriteUser user);
    }

    public String getLogin() {
        return mUser.getLogin();
    }

    public String getAvatar() {
        return mUser.getAvatar();
    }

    public FavoriteUserDetailsViewModel(FavoriteUser user, OnUserShareListener listener) {
        mUser = user;
        mListener = listener;
    }

    public void onUserShared(View v) {
        mListener.onUserShared(mUser);
    }
}
