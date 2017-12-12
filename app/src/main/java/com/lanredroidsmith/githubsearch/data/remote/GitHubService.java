package com.lanredroidsmith.githubsearch.data.remote;

import com.lanredroidsmith.githubsearch.data.remote.model.UserSearchResponse;
import static com.lanredroidsmith.githubsearch.util.MainUtils.ACCEPT_HEADER;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

/**
 * Created by Lanre on 11/6/17.
 */

public interface GitHubService {
    @Headers(ACCEPT_HEADER)
    @GET
    Call<UserSearchResponse> getUsers(@Url String url);
}
