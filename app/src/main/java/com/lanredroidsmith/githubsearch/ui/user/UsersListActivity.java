package com.lanredroidsmith.githubsearch.ui.user;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;

public class UsersListActivity extends AppCompatActivity
        implements UsersListFragment.OnUserSelectedListener {

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            UsersListFragment ulf = UsersListFragment.newInstance(getIntent().getExtras());
            mFragmentManager.beginTransaction().add(R.id.users_list_container, ulf).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onUserSelected(GitHubUser user) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UserDetailsFragment.USER_DETAILS, user);
        mFragmentManager.beginTransaction()
                        .replace(R.id.user_details_container, UserDetailsFragment.newInstance(bundle))
                        .commit();
    }
}