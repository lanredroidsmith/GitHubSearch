package com.lanredroidsmith.githubsearch.ui.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.remote.model.UserSearchResponse;
import com.lanredroidsmith.githubsearch.ui.common.SettingsActivity;
import com.lanredroidsmith.githubsearch.ui.user.loader.UserSearchLoader;
import com.lanredroidsmith.githubsearch.util.MainUtils;
import com.lanredroidsmith.githubsearch.util.SingletonUtil;

import java.util.ArrayList;

import static com.lanredroidsmith.githubsearch.util.MainUtils.NEXT_URL;
import static com.lanredroidsmith.githubsearch.util.MainUtils.FIRST_TOTAL;
import static com.lanredroidsmith.githubsearch.util.MainUtils.TOTAL_ITEMS;
import static com.lanredroidsmith.githubsearch.util.MainUtils.USER_SEARCH_URL;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<UserSearchResponse>
        , SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    Button mBtnSend;
    EditText mTxtUsername, mTxtLanguage, mTxtLocation, mTxtRepos, mTxtFollowers;
    CheckBox mChkMoreOptions;
    RadioButton mRadioIndividual, mRadioOrganization;
    LinearLayout mUserTypeLayout;
    private static final int GET_USERS_LOADER_ID = 1;
    private static final String IS_DIALOG_SHOWING_KEY = "idsk";

    private boolean mIsDialogShowing;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mTxtUsername = (EditText) findViewById(R.id.username);
        mTxtLanguage = (EditText) findViewById(R.id.language);
        mTxtLocation = (EditText) findViewById(R.id.location);
        mTxtRepos = (EditText) findViewById(R.id.repos);
        mTxtFollowers = (EditText) findViewById(R.id.followers);
        mUserTypeLayout = (LinearLayout) findViewById(R.id.usertype_layout);

        setUpPreferences();

        mChkMoreOptions = (CheckBox) findViewById(R.id.more_options);
        mRadioIndividual = (RadioButton) findViewById(R.id.individual);
        mRadioOrganization = (RadioButton) findViewById(R.id.organization);

        mChkMoreOptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTxtRepos.setVisibility(View.VISIBLE);
                    mTxtFollowers.setVisibility(View.VISIBLE);
                    mUserTypeLayout.setVisibility(View.VISIBLE);
                } else {
                    mTxtRepos.setVisibility(View.GONE);
                    mTxtFollowers.setVisibility(View.GONE);
                    mUserTypeLayout.setVisibility(View.GONE);
                }
            }
        });

        mBtnSend = (Button) findViewById(R.id.submit);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareSearch();
            }
        });
    }

    private void setUpPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mTxtLocation.setText(prefs.getString(getString(R.string.pref_user_location_key), ""));
        mTxtLanguage.setText(prefs.getString(getString(R.string.pref_user_lang_key), ""));

        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    private void prepareSearch() {
        String username = mTxtUsername.getText().toString().trim();
        String language = mTxtLanguage.getText().toString().trim();
        String location = mTxtLocation.getText().toString().trim();
        String followers = mTxtFollowers.getText().toString().trim();
        String repos = mTxtRepos.getText().toString().trim();
        if (username.isEmpty() && language.isEmpty() && location.isEmpty() && followers.isEmpty()
                && repos.isEmpty() && !mRadioIndividual.isChecked() && !mRadioOrganization.isChecked()) {
                Toast.makeText(this, getString(R.string.pls_include_param), Toast.LENGTH_SHORT).show();
                return;
        }
        // we init StringBuilder with capacity to avoid series of minute reallocation - #PerfMatters
        // we do append("\"") for language and location cos we may have spaces in between
        StringBuilder sb = new StringBuilder(120);
        sb.append(MainUtils.BASE_URL + "users?q=");
        if (!username.isEmpty()) {
            sb.append(username);
        }
        if (!language.isEmpty()) {
            sb.append(" language:").append("\"").append(language).append("\"");
        }
        if (!location.isEmpty()) {
            sb.append(" location:").append("\"").append(location).append("\"");
        }
        if (!followers.isEmpty()) {
            sb.append(" followers:>=").append(followers);
        }
        if (!repos.isEmpty()) {
            sb.append(" repos:>=").append(repos);
        }
        if (mRadioIndividual.isChecked()) {
            sb.append(" type:user");
        }
        if (mRadioOrganization.isChecked()) {
            sb.append(" type:org");
        }
        sendRequest(sb.toString());
    }

    private void sendRequest(String url) {
        if (!MainUtils.isInternetAvailable(this)) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(USER_SEARCH_URL, url);
        // we use restart because user may search again with a new set of criteria
        getSupportLoaderManager().restartLoader(GET_USERS_LOADER_ID, bundle, this);
        showProgressDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_DIALOG_SHOWING_KEY, mIsDialogShowing);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean(IS_DIALOG_SHOWING_KEY, false))
        {
            showProgressDialog();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite_users_menu:
                startActivity(new Intent(this, FavoriteUsersListActivity.class));
                return true;
            case R.id.settings_menu:
                startActivity(new Intent(this, SettingsActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgressDialog() {
        try {
            // this will be used in saveInstanceState so that we can reshow the dialog
            // when Activity is restarted e.g. in case of config change
            mIsDialogShowing = true;
            // just to be sure that we don't get a WindowLeakedException, so check if
            // the current instance of the Activity is still 'around'
            if (!isFinishing())
            {
                if (mDialog == null)
                {
                    mDialog = new ProgressDialog(this);
                }
                mDialog.setMessage(getString(R.string.dialog_processing));
                mDialog.setCancelable(false);
                mDialog.show();
            }
        } catch (Exception e) { Log.e(TAG, e.getMessage()); }
    }

    // this is for avoiding WindowLeakedException e.g. on config change
    private void closeProgressDialogManually() {
        try {
            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void closeProgressDialog() {
        closeProgressDialogManually();
        mIsDialogShowing = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // in order to avoid WindowLeakedException
        closeProgressDialogManually();
        PreferenceManager.getDefaultSharedPreferences(this)
                         .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public Loader<UserSearchResponse> onCreateLoader(int id, Bundle args) {
        return new UserSearchLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<UserSearchResponse> loader, UserSearchResponse data) {
        closeProgressDialog();
        processResponse(data);
    }

    private void processResponse(UserSearchResponse data) {
        if (data.isSuccessful()) {
            if (!data.getItems().isEmpty()) {
                Intent intent = new Intent(this, UsersListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(TOTAL_ITEMS, data.getTotalCount());
                bundle.putInt(FIRST_TOTAL, data.getItems().size());
                bundle.putString(NEXT_URL, data.getNextUrl());
                // we put the list in a Singleton to be retrieved in the
                // list fragment - to avoid TransactionTooLargeException
                SingletonUtil.getInstance().setUsers(new ArrayList<>(data.getItems()));
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.empty_results), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, data.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<UserSearchResponse> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.pref_user_location_key).equals(key)) {
            mTxtLocation.setText(sharedPreferences.getString(key, ""));
        } else if (getString(R.string.pref_user_lang_key).equals(key)) {
            mTxtLanguage.setText(sharedPreferences.getString(key, ""));
        }
    }
}
