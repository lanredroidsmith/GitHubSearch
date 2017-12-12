package com.lanredroidsmith.githubsearch.ui.user;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.local.model.FavoriteUser;
import com.lanredroidsmith.githubsearch.data.local.repo.UserRepository;
import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;
import com.lanredroidsmith.githubsearch.data.remote.model.UserSearchResponse;
import com.lanredroidsmith.githubsearch.ui.common.PaginationScrollListener;
import com.lanredroidsmith.githubsearch.ui.common.SingleChoiceMode;
import com.lanredroidsmith.githubsearch.ui.user.adapter.UserAdapter;
import com.lanredroidsmith.githubsearch.ui.user.loader.UserSearchLoader;
import com.lanredroidsmith.githubsearch.ui.user.viewmodel.UserListItemViewModel;
import com.lanredroidsmith.githubsearch.util.MainUtils;
import com.lanredroidsmith.githubsearch.util.SingletonUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.lanredroidsmith.githubsearch.util.MainUtils.CURRENT_POSITION_KEY;
import static com.lanredroidsmith.githubsearch.util.MainUtils.CURRENT_TOTAL_KEY;
import static com.lanredroidsmith.githubsearch.util.MainUtils.FAVORITE_USERS_FOLDER_NAME;
import static com.lanredroidsmith.githubsearch.util.MainUtils.FIRST_TOTAL;
import static com.lanredroidsmith.githubsearch.util.MainUtils.IS_LOADING_KEY;
import static com.lanredroidsmith.githubsearch.util.MainUtils.NEXT_URL;
import static com.lanredroidsmith.githubsearch.util.MainUtils.NEXT_URL_KEY;
import static com.lanredroidsmith.githubsearch.util.MainUtils.TOTAL_ITEMS;
import static com.lanredroidsmith.githubsearch.util.MainUtils.TOTAL_ITEMS_KEY;
import static com.lanredroidsmith.githubsearch.util.MainUtils.USER_SEARCH_URL;

public class UsersListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<UserSearchResponse>,
        UserListItemViewModel.OnUserInteractionListener, OnUsersReadFromFileListener {

    private static final int GET_USERS_LOADER_ID = 1, GET_USER_LOADER_ID = 3;
    private static final int DEFAULT_CHECKED_POSITION = -1; // just to indicate no selection
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final String CURRENT_FAV_USER_KEY = "cfuk";
    private static final String PERMISSION_REASON_SHOWING_KEY = "prsk";
    private static final String TAG = UsersListFragment.class.getSimpleName();

    private OnUserSelectedListener mListener;
    private RecyclerView mRecyclerView;
    private UserAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<GitHubUser> mUsers;
    private boolean mDualPane;
    private int mSelectedPosition = DEFAULT_CHECKED_POSITION;
    private int mTotalItems;
    private int mCurrentTotal;
    private boolean mIsLoading;
    private String mNextUrl;
    private Context mContext;
    // this is majorly to track if eg the user wants to add a user as favorite but the app is awaiting
    // permission, without this, if the device is rotated e.g., we lose that user
    private GitHubUser mCurrentFavoriteUser;
    private boolean mIsPermissionReasonShowing;
    private AlertDialog mPermissionReasonDialog;

    public UsersListFragment() {
        // Required empty public constructor
    }

    public static UsersListFragment newInstance(Bundle bundle) {
        UsersListFragment fragment = new UsersListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        if (savedInstanceState == null) {
            // it's the first time
            if (getArguments() != null) {
                Bundle bundle = getArguments();
                mCurrentTotal = bundle.getInt(FIRST_TOTAL);
                mTotalItems = bundle.getInt(TOTAL_ITEMS);
                mNextUrl = bundle.getString(NEXT_URL);
            } else {
                // finish the parent (Activity) - sth very strange must have happened, unlikely though
                Toast.makeText(mContext, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        } else {
            mSelectedPosition = savedInstanceState.getInt(CURRENT_POSITION_KEY,
                    DEFAULT_CHECKED_POSITION);
            mIsLoading = savedInstanceState.getBoolean(IS_LOADING_KEY);
            mTotalItems = savedInstanceState.getInt(TOTAL_ITEMS_KEY);
            mCurrentTotal = savedInstanceState.getInt(CURRENT_TOTAL_KEY);
            mNextUrl = savedInstanceState.getString(NEXT_URL_KEY);
            mCurrentFavoriteUser = savedInstanceState.getParcelable(CURRENT_FAV_USER_KEY);
            mIsPermissionReasonShowing = savedInstanceState.getBoolean(PERMISSION_REASON_SHOWING_KEY);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.user_details_container);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (mDualPane) {
            mAdapter.setChoiceMode(new SingleChoiceMode());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveUsersInstanceState();
        outState.putInt(CURRENT_POSITION_KEY, mSelectedPosition);
        outState.putInt(TOTAL_ITEMS_KEY, mTotalItems);
        outState.putInt(CURRENT_TOTAL_KEY, mCurrentTotal);
        outState.putString(NEXT_URL_KEY, mNextUrl);
        outState.putBoolean(IS_LOADING_KEY, mIsLoading);
        outState.putParcelable(CURRENT_FAV_USER_KEY, mCurrentFavoriteUser);
        outState.putBoolean(PERMISSION_REASON_SHOWING_KEY, mIsPermissionReasonShowing);
        // In dual-pane mode, our list tracks the selected item.
        if (mDualPane) { mAdapter.onSaveInstanceState(outState); }
    }

    private void saveUsersInstanceState() {
        SingletonUtil.getInstance().setUsers(mUsers);
        MainUtils.writeUsersToFile(new WeakReference<>(mContext), mUsers);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // In dual-pane mode, our list highlights the selected item.
        if (savedInstanceState != null && mDualPane) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
        }

        if (mIsPermissionReasonShowing) {
            showPermissionReason();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // release memory
        if (getActivity().isFinishing()) {
            SingletonUtil.getInstance().setUsers(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container,
                false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.users_list);

        // we always retrieve the list of users from our singleton as we can't
        // use savedInstanceState - to avoid TransactionTooLargeException
        mUsers = SingletonUtil.getInstance().getUsers();
        mAdapter = new UserAdapter(mUsers, this, mContext, mRecyclerView);
        if (mUsers == null) {
            // we must have come from process death, so read from file
            MainUtils.getUsersFromFile(new WeakReference<>(mContext),
                    new WeakReference<OnUsersReadFromFileListener>(this),
                    new WeakReference<>(savedInstanceState));
        }

        // in order to help keep the loading status of the adapter e.g. on config change
        mAdapter.setIsLoading(mIsLoading);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext,
                DividerItemDecoration.VERTICAL_LIST));
        addOnScrollListener();

        return view;
    }

    private void addOnScrollListener() {
        mRecyclerView.addOnScrollListener(
                new PaginationScrollListener<LinearLayoutManager>(mLinearLayoutManager, 0) {
            @Override
            protected void loadMoreItems() {
                if (!mIsLoading) loadMore();
            }
        });
    }

    public void loadMore() {
        if (!MainUtils.isInternetAvailable(mContext)) {
            Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show();
            return;
        }
        if ( mNextUrl != null && !mNextUrl.isEmpty() && mCurrentTotal < mTotalItems ) {
            mIsLoading = true;
            mAdapter.addLoading();
            Bundle bundle = new Bundle();
            bundle.putString(USER_SEARCH_URL, mNextUrl);
            // we use restart because we must contact the server for every next page load
            getActivity().getSupportLoaderManager().restartLoader(GET_USERS_LOADER_ID, bundle, this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserSelectedListener) {
            mListener = (OnUserSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnUserInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPermissionReasonDialog != null)
            mPermissionReasonDialog.dismiss();
    }

    @Override
    public void onUserSelected(GitHubUser user) {
        // if the currently selected is selected again & we're in dual mode, do nothing
        if (mSelectedPosition == mUsers.indexOf(user) && mDualPane) { return; }
        mSelectedPosition = mUsers.indexOf(user);
        if (mDualPane) {
            mAdapter.setItemChecked(mSelectedPosition, true);
            mListener.onUserSelected(user);
        } else {
            Intent intent = new Intent(mContext, UserDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(UserDetailsFragment.USER_DETAILS, user);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void addFavorite(GitHubUser user) {
        mCurrentFavoriteUser = user;
        getActivity().getSupportLoaderManager().restartLoader(GET_USER_LOADER_ID, null,
            new LoaderManager.LoaderCallbacks<Cursor>() {
                boolean done = false;
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    return new UserRepository().getUserByLogin(mContext, mCurrentFavoriteUser.getLogin());
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    if (done) {
                        return;
                    }
                    if (data.getCount() == 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            checkWriteStoragePermission();
                        } else {
                            // for pre-Marshmallow, permission is already granted at install time,
                            // so we can save avatar
                            addFavorite(true);
                        }
                    } else {
                        Toast.makeText(mContext, R.string.user_already_favorite, Toast.LENGTH_SHORT).show();
                    }
                    done = true;
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
        });
    }

    @Override
    public void removeFavorite(GitHubUser user) {
        // delete avatar
        MainUtils.deleteAvatar(mContext, user.getLogin() + ".png");
        new UserRepository().removeUserFromFavorites(mContext, user);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkWriteStoragePermission() {
        if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showPermissionReason();
                return;
            }
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORAGE_PERMISSION_REQUEST_CODE);
            return;
        }
        addFavorite(true); // executed ONLY when permission is granted
    }

    private void showPermissionReason() {
        mIsPermissionReasonShowing = true;
        mPermissionReasonDialog = new AlertDialog.Builder(mContext)
                .setMessage(getString(R.string.write_storage_reason, getString(R.string.app_name)))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok_btn_label),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mIsPermissionReasonShowing = false;
                            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    WRITE_STORAGE_PERMISSION_REQUEST_CODE);
                        }
                }).create();
        mPermissionReasonDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_STORAGE_PERMISSION_REQUEST_CODE:
                /*
                *  doc says: It is possible that the permissions request interaction with the user
                *  is interrupted. In this case you will receive empty permissions and results
                *  arrays which should be treated as a cancellation.
                *
                *  So, we make the request again.
                * */
                if (grantResults.length == 0 || permissions.length == 0) {
                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_STORAGE_PERMISSION_REQUEST_CODE);
                    return;
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addFavorite(true);
                } else {
                    Toast.makeText(mContext, R.string.permission_denied, Toast.LENGTH_LONG).show();
                    addFavorite(false);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void addFavorite(boolean saveAvatar){
        FavoriteUser fu = new FavoriteUser();
        fu.setLogin(mCurrentFavoriteUser.getLogin());
        if (saveAvatar) {
            View rootView = mRecyclerView
                            .findViewHolderForAdapterPosition(mUsers.indexOf(mCurrentFavoriteUser))
                            .itemView;
            if (null == rootView) {
                // VERY unlikely though :)
                Toast.makeText(mContext, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                return;
            }
            ImageView avatar = (ImageView) rootView.findViewById(R.id.user_avatar);
            Drawable d = avatar.getDrawable();
            if (d != null) {
                if (!saveUserAvatar(mCurrentFavoriteUser, d))
                    return;
                fu.setAvatar(fu.getLogin() + ".png");
            } else {
                /*
                *  when the image is yet to be loaded by Picasso - maybe slow network
                *  still go ahead and persist, we just notify the user
                * */
                Toast.makeText(mContext, R.string.avatar_not_found, Toast.LENGTH_SHORT).show();
            }
        }

        fu.setHtmlUrl(mCurrentFavoriteUser.getHtmlUrl());
        fu.setType(mCurrentFavoriteUser.getType());
        new UserRepository().addUserToFavorites(mContext, fu);
    }

    private boolean saveUserAvatar(GitHubUser user, Drawable d) {
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_SHARED.equals(storageState)) {
            Toast.makeText(mContext, R.string.storage_mounted, Toast.LENGTH_SHORT).show();
            return false;
        } else if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ((BitmapDrawable)d).getBitmap().compress(Bitmap.CompressFormat.PNG, 80, baos);
                byte[] imgArray = baos.toByteArray();
                if (imgArray != null && imgArray.length > 0) {
                    String favoriteUsersDir = Environment.getExternalStorageDirectory() + File.separator +
                            getString(R.string.app_name) + File.separator + FAVORITE_USERS_FOLDER_NAME;
                    File folder = new File(favoriteUsersDir);
                    if (!folder.exists() || !folder.isDirectory())
                    {
                        if (!folder.mkdirs())
                        {
                            // abort the operation
                            throw new Exception("could not create folder to store avatar");
                        }
                    }

                    String filename = user.getLogin() + ".png";
                    File saveFile = new File(favoriteUsersDir, filename);
                    if (saveFile.exists())
                    {
                        // delete it off, we want something fresh :). we wonder how it even got there
                        if (!saveFile.delete())
                        {
                            throw new Exception("could not save avatar");
                        }
                    }

                    FileOutputStream stream = new FileOutputStream(saveFile);
                    stream.write(imgArray);
                    stream.flush();
                    stream.close();

                    return true;
                } else {
                    Toast.makeText(mContext, R.string.could_not_save_avatar, Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(mContext, R.string.could_not_save_avatar, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(mContext, R.string.storage_not_found, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onUsersRead(ArrayList<GitHubUser> users, WeakReference<Bundle> bundle) {
        SingletonUtil.getInstance().setUsers(users);
        /*if (mDualPane && bundle.get() != null) {
            mAdapter.onRestoreInstanceState(bundle.get());
        }*/
        mAdapter.setData(users);
    }

    interface OnUserSelectedListener {
        void onUserSelected(GitHubUser user);
    }

    @Override
    public Loader<UserSearchResponse> onCreateLoader(int id, Bundle args) {
        return new UserSearchLoader(mContext, args);
    }

    @Override
    public void onLoadFinished(Loader<UserSearchResponse> loader, UserSearchResponse data) {
        // we remove the loading ProgressBar of the RecyclerView 1st before adding more (if there is)
        // this also removes that empty user added to the users list for the sake of the ProgressBar
        mAdapter.removeLoading();
        processResponse(data);
    }

    private void processResponse(UserSearchResponse data) {
        if (data.isSuccessful()) {
            if (!data.getItems().isEmpty()) {
                ArrayList<GitHubUser> users = new ArrayList<>(data.getItems());
                mCurrentTotal += users.size();
                mTotalItems = data.getTotalCount(); // cos new users might have just been added
                mNextUrl =  data.getNextUrl();
                mAdapter.addMoreUsers(users);
            } else {
                Toast.makeText(mContext, R.string.empty_results, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mContext, data.getMessage(), Toast.LENGTH_LONG).show();
        }
        mIsLoading = false;
    }

    @Override
    public void onLoaderReset(Loader<UserSearchResponse> loader) {

    }
}
