package com.lanredroidsmith.githubsearch.ui.common;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.lanredroidsmith.githubsearch.R;

/**
 * Created by Lanre on 11/30/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_user);

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences prefs = preferenceScreen.getSharedPreferences();

        int count = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = preferenceScreen.getPreference(i);
            if (p instanceof PreferenceCategory) {
                PreferenceCategory category = (PreferenceCategory)p;
                int catCount = category.getPreferenceCount();
                for (int j = 0; j < catCount; j++) {
                    Preference catPref = category.getPreference(j);
                    setPrefSummary(prefs, catPref, catPref.getKey());
                }
            } else
                setPrefSummary(prefs, p, p.getKey());
        }
    }

    private void setPrefSummary(SharedPreferences prefs, Preference p, String key) {
        // all the preferences we have for now are EditTexts, so no need doing instanceof
        if ( key.equals(getString(R.string.pref_user_lang_key)) ) {
            p.setSummary(prefs.getString(key,
                    getString(R.string.pref_user_lang_default_summary)));
        } else if ( key.equals(getString(R.string.pref_user_location_key)) ) {
            p.setSummary(prefs.getString(key,
                    getString(R.string.pref_user_location_default_summary)));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);
        if (p != null) {
            setPrefSummary(sharedPreferences, p, key);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}