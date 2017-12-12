package com.lanredroidsmith.githubsearch;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Lanre on 11/24/17.
 */

public class GitHubSearchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
