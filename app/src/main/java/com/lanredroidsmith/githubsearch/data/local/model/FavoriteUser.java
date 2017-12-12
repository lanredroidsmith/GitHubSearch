package com.lanredroidsmith.githubsearch.data.local.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Lanre on 11/23/17.
 */

public class FavoriteUser implements Parcelable, Serializable {
    private int id;
    private String login;

    public FavoriteUser() {}

    public FavoriteUser(int id, String login, String htmlUrl, String type, String avatar) {
        this.id = id;
        this.login = login;
        this.htmlUrl = htmlUrl;
        this.type = type;
        this.avatar = avatar;
    }

    protected FavoriteUser(Parcel in) {
        id = in.readInt();
        login = in.readString();
        htmlUrl = in.readString();
        type = in.readString();
        avatar = in.readString();
    }

    public static final Creator<FavoriteUser> CREATOR = new Creator<FavoriteUser>() {
        @Override
        public FavoriteUser createFromParcel(Parcel in) {
            return new FavoriteUser(in);
        }

        @Override
        public FavoriteUser[] newArray(int size) {
            return new FavoriteUser[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    private String htmlUrl;
    private String type;
    private String avatar;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(login);
        dest.writeString(htmlUrl);
        dest.writeString(type);
        dest.writeString(avatar);
    }
}
