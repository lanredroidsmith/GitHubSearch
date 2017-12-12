package com.lanredroidsmith.githubsearch.ui.user;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.lanredroidsmith.githubsearch.R;

/**
 * Created by Lanre on 11/28/17.
 */

public class FavoriteUserDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.user_details_container,
                            FavoriteUserDetailsFragment.newInstance(getIntent().getExtras()))
                    .commit();
        }
    }
}