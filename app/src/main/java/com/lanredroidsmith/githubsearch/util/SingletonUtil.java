package com.lanredroidsmith.githubsearch.util;

import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;

import java.util.ArrayList;

/**
 * Created by Lanre on 11/27/17.
 */

public class SingletonUtil {

    private static SingletonUtil singleton;

    private ArrayList<GitHubUser> users;

    public ArrayList<GitHubUser> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<GitHubUser> users) {
        this.users = users;
    }

    public static SingletonUtil getInstance() {
        if (singleton == null) {
            singleton = new SingletonUtil();
        }
        return singleton;
    }
}
