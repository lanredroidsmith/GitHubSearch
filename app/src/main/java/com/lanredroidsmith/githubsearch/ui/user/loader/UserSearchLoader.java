package com.lanredroidsmith.githubsearch.ui.user.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.lanredroidsmith.githubsearch.data.remote.model.UserSearchResponse;
import com.lanredroidsmith.githubsearch.data.remote.GitHubService;
import com.lanredroidsmith.githubsearch.data.remote.RetrofitClient;
import com.lanredroidsmith.githubsearch.util.MainUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.lanredroidsmith.githubsearch.util.MainUtils.USER_SEARCH_URL;

/**
 * Created by Lanre on 11/9/17.
 */

public class UserSearchLoader extends Loader<UserSearchResponse> {
    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     */
    public UserSearchLoader(Context context, Bundle bundle) {
        super(context);
        mBundle = bundle;
    }

    private UserSearchResponse mData;
    private Bundle mBundle;

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            GitHubService service = RetrofitClient.getClient().create(GitHubService.class);
            service.getUsers(mBundle.getString(USER_SEARCH_URL)).enqueue(new Callback<UserSearchResponse>() {
                @Override
                public void onResponse(Call<UserSearchResponse> call, Response<UserSearchResponse> response) {
                    UserSearchResponse usr;
                    usr = response.body();
                    if (response.isSuccessful() && usr != null && usr.getItems() != null) {
                        usr.setNextUrl(MainUtils.getNextUrl(response.headers().get("link")));
                        usr.setSuccessful(true);
                    } else {
                        usr = new UserSearchResponse();
                        usr.setSuccessful(false);
                        String message = "Could not get users. Please try again.";
                        if (response.code() == 403) {
                            message = "Rate limit exceeded. Please retry after a minute.";
                        } else {
                            message = response.message() == null ? message : response.message();
                        }
                        usr.setMessage(message);
                    }
                    deliverResult(usr);
                }

                @Override
                public void onFailure(Call<UserSearchResponse> call, Throwable t) {
                    UserSearchResponse usr = new UserSearchResponse();
                    usr.setSuccessful(false);
                    usr.setMessage(t.getMessage());
                    deliverResult(usr);
                }
            });
        }
    }

    @Override
    public void deliverResult(UserSearchResponse data) {
        mData = data;
        super.deliverResult(data);
    }
}
