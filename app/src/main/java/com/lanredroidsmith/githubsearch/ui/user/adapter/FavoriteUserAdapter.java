package com.lanredroidsmith.githubsearch.ui.user.adapter;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.local.DbContract;
import com.lanredroidsmith.githubsearch.data.local.model.FavoriteUser;
import com.lanredroidsmith.githubsearch.databinding.FavoriteUserItemBinding;
import com.lanredroidsmith.githubsearch.ui.common.ChoiceCapableAdapter;
import com.lanredroidsmith.githubsearch.ui.common.SingleChoiceMode;
import com.lanredroidsmith.githubsearch.ui.user.viewmodel.FavoriteUserListItemViewModel;

/**
 * Created by Lanre on 11/28/17.
 */

public class FavoriteUserAdapter extends ChoiceCapableAdapter<FavoriteUserAdapter.BindingHolder> {
    private Context mContext;
    private Cursor mCursor;
    private FavoriteUserListItemViewModel.OnUserInteractionListener mListener;

    private int mIdIndex, mLoginIndex, mAvatarIndex, mTypeIndex, mUrlIndex;

    public FavoriteUserAdapter(FavoriteUserListItemViewModel.OnUserInteractionListener listener,
                       Context context, RecyclerView rv) {
        super(rv);
        mListener = listener;
        mContext = context;
        mRecyclerView = rv;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FavoriteUserItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                R.layout.favorite_user_item, parent, false);
        return new BindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        mCursor.moveToPosition(position); // get to the right location in the cursor

        // Determine the values of the wanted data
        int id = mCursor.getInt(mIdIndex);
        String login = mCursor.getString(mLoginIndex);
        String avatar = mCursor.getString(mAvatarIndex);
        String type = mCursor.getString(mTypeIndex);
        String url = mCursor.getString(mUrlIndex);

        FavoriteUserItemBinding binding = holder.binding;
        holder.itemView.setTag(login); // this is what we use to delete on swipe
        binding.setUvm(new FavoriteUserListItemViewModel(
                new FavoriteUser(id, login, url, type, avatar), mListener, position));
        if (mChoiceMode != null) {
            binding.userCard.setActivated(isChecked(position));
        }
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public void setItemChecked(int position, boolean isChecked) {
        if (mChoiceMode == null) { return; }
        if (mChoiceMode.isSingleChoice()) {
            int checked= mChoiceMode.getCheckedPosition(); // get the currently selected position

            if (checked >= 0) {
                BindingHolder row =
                        (BindingHolder)mRecyclerView.findViewHolderForAdapterPosition(checked);
                if (row != null) {
                    row.setChecked(false);
                }
            }
        }

        // now activate the newly selected row
        BindingHolder row =
                (BindingHolder)mRecyclerView.findViewHolderForAdapterPosition(position);
        if (row != null) {
            row.setChecked(true);
        }

        mChoiceMode.setChecked(position, isChecked); // this keeps track of the selected position
    }

    @Override
    public void onViewAttachedToWindow(BindingHolder holder) {
        super.onViewAttachedToWindow(holder);
        // this is to clear selection from a reused view that was selected
        if (mChoiceMode != null && mChoiceMode instanceof SingleChoiceMode) {
            int position = holder.getAdapterPosition();
            if (position != mChoiceMode.getCheckedPosition()) {
                holder.setChecked(false);
            }
        }
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the data set
        if (c != null) {
            getColumnIndices(mCursor);
            notifyDataSetChanged();
        }

        return temp;
    }

    private void getColumnIndices(Cursor mCursor) {
        // Indices for the columns
        mIdIndex = mCursor.getColumnIndex(DbContract.FavoriteUsers._ID);
        mLoginIndex = mCursor.getColumnIndex(DbContract.FavoriteUsers.COLUMN_LOGIN);
        mAvatarIndex = mCursor.getColumnIndex(DbContract.FavoriteUsers.COLUMN_AVATAR);
        mTypeIndex = mCursor.getColumnIndex(DbContract.FavoriteUsers.COLUMN_TYPE);
        mUrlIndex = mCursor.getColumnIndex(DbContract.FavoriteUsers.COLUMN_HTML_URL);
    }

    public class BindingHolder extends RecyclerView.ViewHolder {
        FavoriteUserItemBinding binding;

        BindingHolder(FavoriteUserItemBinding binding) {
            super(binding.userCard);
            this.binding = binding;
        }

        void setChecked(boolean isChecked) {
            binding.userCard.setActivated(isChecked);
        }
    }
}
