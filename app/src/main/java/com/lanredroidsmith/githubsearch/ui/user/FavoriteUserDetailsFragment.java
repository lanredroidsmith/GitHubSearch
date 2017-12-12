package com.lanredroidsmith.githubsearch.ui.user;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.local.model.FavoriteUser;
import com.lanredroidsmith.githubsearch.databinding.FragmentFavoriteUserDetailsBinding;
import com.lanredroidsmith.githubsearch.ui.user.viewmodel.FavoriteUserDetailsViewModel;


public class FavoriteUserDetailsFragment extends Fragment
                implements FavoriteUserDetailsViewModel.OnUserShareListener {

    public static final String USER_DETAILS = "details";
    private Context mContext;
    private FavoriteUser mUser;

    public FavoriteUserDetailsFragment() {
        // Required empty public constructor
    }

    public static FavoriteUserDetailsFragment newInstance(Bundle bundle) {
        FavoriteUserDetailsFragment fragment = new FavoriteUserDetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        if (savedInstanceState == null) {
            if (getArguments() != null) {
                Bundle bundle = getArguments();
                mUser = bundle.getParcelable(USER_DETAILS);
            } else {
                // finish the parent (Activity) - sth very strange must have happened, unlikely though
                Toast.makeText(mContext, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        } else {
            mUser = savedInstanceState.getParcelable(USER_DETAILS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentFavoriteUserDetailsBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext), R.layout.fragment_favorite_user_details, container, false);
        binding.setUvm(new FavoriteUserDetailsViewModel(mUser, this));
        return binding.detailsPanel;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(USER_DETAILS, mUser);
    }

    @Override
    public void onUserShared(FavoriteUser user) {
        shareThisUser(user);
    }

    private void shareThisUser(FavoriteUser user) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareBody = getString(R.string.share_user_msg_body, user.getLogin(), user.getHtmlUrl());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_user_msg_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        // we must check the availability of an app that can handle our Intent
        if (shareIntent.resolveActivity(mContext.getPackageManager()) != null)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_dialog_title)));
        else
            Toast.makeText(mContext, getString(R.string.no_share_app_found), Toast.LENGTH_SHORT).show();
    }
}