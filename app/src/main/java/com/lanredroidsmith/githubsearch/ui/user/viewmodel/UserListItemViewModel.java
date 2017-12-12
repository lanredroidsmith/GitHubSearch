package com.lanredroidsmith.githubsearch.ui.user.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.lanredroidsmith.githubsearch.BR;
import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;

/**
 * Created by Lanre on 11/8/17.
 */

public class UserListItemViewModel extends BaseObservable {

    private GitHubUser mUser;
    private OnUserInteractionListener mListener;
    private Context mContext;

    public interface OnUserInteractionListener {
        void onUserSelected(GitHubUser user);
        void addFavorite(GitHubUser user);
        void removeFavorite(GitHubUser user);
    }

    public UserListItemViewModel(GitHubUser user, OnUserInteractionListener listener, Context context) {
        mUser = user;
        mListener = listener;
        mContext = context;
    }

    public String getLogin() {
        return mUser.getLogin();
    }

    public String getAvatarUrl() {
        return mUser.getAvatarUrl();
    }

    @Bindable
    public Drawable getFavorite() {
       return mUser.isFavorite() ? ContextCompat.getDrawable(mContext, R.drawable.star_filled_48) :
               ContextCompat.getDrawable(mContext, R.drawable.star_48);
    }

    private void setFavorite(boolean isFavorite) {
        if (isFavorite)
            mListener.addFavorite(mUser);
        else
            mListener.removeFavorite(mUser);
        mUser.setFavorite(isFavorite);
        notifyPropertyChanged(BR.favorite);
    }

    public void onFavoriteClicked(View v) {
        setFavorite(!mUser.isFavorite());
    }

    public void onUserSelected(View v) {
        mListener.onUserSelected(mUser);
    }
}
