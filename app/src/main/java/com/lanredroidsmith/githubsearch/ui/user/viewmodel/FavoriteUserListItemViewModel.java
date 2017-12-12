package com.lanredroidsmith.githubsearch.ui.user.viewmodel;

import android.view.View;

import com.lanredroidsmith.githubsearch.data.local.model.FavoriteUser;

/**
 * Created by Lanre on 11/28/17.
 */

public class FavoriteUserListItemViewModel {
    private FavoriteUser mUser;
    private OnUserInteractionListener mListener;
    private int mAdapterPosition; // so that we know what to highlight as selected in dual pane mode

    public interface OnUserInteractionListener {
        void onUserSelected(FavoriteUser user, int position);
    }

    public FavoriteUserListItemViewModel(FavoriteUser user, OnUserInteractionListener listener,
                                         int position) {
        mUser = user;
        mListener = listener;
        mAdapterPosition = position;
    }

    public String getLogin() {
        return mUser.getLogin();
    }

    public String getAvatar() {
        return mUser.getAvatar();
    }

    public void onUserSelected(View v) {
        mListener.onUserSelected(mUser, mAdapterPosition);
    }
}
