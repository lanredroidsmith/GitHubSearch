package com.lanredroidsmith.githubsearch.ui.user;

import android.os.Bundle;

import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Lanre on 11/27/17.
 */

public interface OnUsersReadFromFileListener {
    void onUsersRead(ArrayList<GitHubUser> users, WeakReference<Bundle> bundle);
}
