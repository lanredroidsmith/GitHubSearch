package com.lanredroidsmith.githubsearch.ui.user;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.local.model.FavoriteUser;

/**
 * Created by Lanre on 11/28/17.
 */

public class FavoriteUsersListActivity extends AppCompatActivity
        implements FavoriteUsersListFragment.OnUserInteractionListener {

    private static final String DETAILS_FRAGMENT_TAG = "detailsfrag";
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            FavoriteUsersListFragment fulf = FavoriteUsersListFragment.newInstance();
            mFragmentManager.beginTransaction().add(R.id.users_list_container, fulf).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onUserSelected(FavoriteUser user) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(FavoriteUserDetailsFragment.USER_DETAILS, user);
        mFragmentManager.beginTransaction()
                .replace(R.id.user_details_container,
                        FavoriteUserDetailsFragment.newInstance(bundle),
                        DETAILS_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onDeleteSelectedUser() {
        mFragmentManager.beginTransaction()
                        .remove(mFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG)).commit();
    }
}