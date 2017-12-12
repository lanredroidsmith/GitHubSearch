package com.lanredroidsmith.githubsearch.ui.user.viewmodel;

import android.view.View;

import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;

/**
 * Created by Lanre on 11/11/17.
 */

public class UserDetailsViewModel {
    private GitHubUser mUser;
    private OnUserShareListener mListener;

    public interface OnUserShareListener {
        void onUserShared(GitHubUser user);
    }

    public String getLogin() {
        return mUser.getLogin();
    }

    public String getAvatarUrl() {
        return mUser.getAvatarUrl();
    }

    public UserDetailsViewModel(GitHubUser user, OnUserShareListener listener) {
        mUser = user;
        mListener = listener;
    }

    public void onUserShared(View v) {
        mListener.onUserShared(mUser);
    }
}
