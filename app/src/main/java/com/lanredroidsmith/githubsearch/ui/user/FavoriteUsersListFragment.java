package com.lanredroidsmith.githubsearch.ui.user;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.local.model.FavoriteUser;
import com.lanredroidsmith.githubsearch.data.local.repo.UserRepository;
import com.lanredroidsmith.githubsearch.ui.common.SingleChoiceMode;
import com.lanredroidsmith.githubsearch.ui.user.adapter.FavoriteUserAdapter;
import com.lanredroidsmith.githubsearch.ui.user.viewmodel.FavoriteUserListItemViewModel;
import com.lanredroidsmith.githubsearch.util.MainUtils;

import static com.lanredroidsmith.githubsearch.util.MainUtils.CURRENT_POSITION_KEY;

public class FavoriteUsersListFragment extends Fragment
        implements FavoriteUserListItemViewModel.OnUserInteractionListener,
                   LoaderManager.LoaderCallbacks<Cursor> {

    private OnUserInteractionListener mListener;
    private Context mContext;
    private int mSelectedPosition = DEFAULT_CHECKED_POSITION;
    private static final int DEFAULT_CHECKED_POSITION = -1; // just to indicate no selection

    // mIsRecreated helps to track if we just entered into the fragment
    private boolean mDualPane, mIsRecreated;
    private FavoriteUserAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private static final int FAVORITE_USERS_LOADER_ID = 1;

    public FavoriteUsersListFragment() {
        // Required empty public constructor
    }

    public static FavoriteUsersListFragment newInstance() {
        return new FavoriteUsersListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        if (savedInstanceState == null) {
            if (getArguments() != null) {
                // do sth
            }
        } else {
            mSelectedPosition = savedInstanceState.getInt(CURRENT_POSITION_KEY,
                    DEFAULT_CHECKED_POSITION);
            mIsRecreated = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container,
                false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.users_list);
        addItemTouchHelper();
        /*
        *  we retrieve the list of favorite users using a Loader.
        *  note that we use initLoader() here NOT restartLoader()
        * */
        getActivity().getSupportLoaderManager().initLoader(FAVORITE_USERS_LOADER_ID, null, this);
        mAdapter = new FavoriteUserAdapter(this, mContext, mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext,
                DividerItemDecoration.VERTICAL_LIST));

        return view;
    }

    private void addItemTouchHelper() {
        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Retrieve the id of the user to delete
                showUndoSnackBar((String) viewHolder.itemView.getTag(), viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    private void showUndoSnackBar(final String login, final int adapterPosition) {
        Snackbar undoSnackBar = Snackbar.make(getView().findViewById(R.id.coordinatorLayout),
                R.string.list_item_deleted, Snackbar.LENGTH_LONG);
        undoSnackBar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
        undoSnackBar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                switch (event) {
                    case DISMISS_EVENT_TIMEOUT:
                    case DISMISS_EVENT_SWIPE:
                    case DISMISS_EVENT_CONSECUTIVE:
                    case DISMISS_EVENT_MANUAL: /* this is the case if we suddenly rotate */
                        if (mDualPane && mSelectedPosition == adapterPosition) {
                            // reset the selected position to default and remove the details fragment
                            mSelectedPosition = DEFAULT_CHECKED_POSITION;
                            ((SingleChoiceMode)mAdapter.getChoiceMode()).clearSelection();
                            mListener.onDeleteSelectedUser();
                        }
                        new UserRepository().deleteFavoriteUser(mContext, login);
                        MainUtils.deleteAvatar(mContext, login + ".png");
                        // Restart the loader in order to have a new set of data
                        if (getActivity() != null) {
                            // this is important particularly for when it's DISMISS_EVENT_MANUAL due
                            // to rotation/other config change(s). Else, you get NPE.
                            getActivity().getSupportLoaderManager().restartLoader(FAVORITE_USERS_LOADER_ID,
                                    null, FavoriteUsersListFragment.this);
                        }
                        break;
                    case DISMISS_EVENT_ACTION:
                        mAdapter.notifyItemChanged(adapterPosition);
                        break;
                }
            }
        });
        undoSnackBar.show();
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
        outState.putInt(CURRENT_POSITION_KEY, mSelectedPosition);
        // In dual-pane mode, our list tracks the selected item.
        if (mDualPane) { mAdapter.onSaveInstanceState(outState); }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // In dual-pane mode, our list highlights the selected item.
        if (savedInstanceState != null && mDualPane) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserInteractionListener) {
            mListener = (OnUserInteractionListener) context;
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
    public void onUserSelected(FavoriteUser user, int position) {
        // if the currently selected is selected again & we're in dual mode, do nothing
        if (mSelectedPosition == position && mDualPane) { return; }
        mSelectedPosition = position;
        if (mDualPane) {
            mAdapter.setItemChecked(mSelectedPosition, true);
            mListener.onUserSelected(user);
        } else {
            Intent intent = new Intent(mContext, FavoriteUserDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(FavoriteUserDetailsFragment.USER_DETAILS, user);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new UserRepository().getAllFavoriteUsers(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // we use 'mIsRecreated' to avoid e.g. reshowing the Toast
        // upon rotations by the user while we have empty result
        if (data.getCount() == 0 && !mIsRecreated) {
            Toast.makeText(mContext, R.string.no_favorite_users, Toast.LENGTH_SHORT).show();
        } else {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    interface OnUserInteractionListener {
        void onUserSelected(FavoriteUser user);
        void onDeleteSelectedUser();
    }

}