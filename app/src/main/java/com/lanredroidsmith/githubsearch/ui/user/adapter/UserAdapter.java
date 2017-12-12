package com.lanredroidsmith.githubsearch.ui.user.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lanredroidsmith.githubsearch.R;
import com.lanredroidsmith.githubsearch.data.remote.model.GitHubUser;
import com.lanredroidsmith.githubsearch.databinding.UserItemBinding;
import com.lanredroidsmith.githubsearch.ui.common.ChoiceCapableAdapter;
import com.lanredroidsmith.githubsearch.ui.common.SingleChoiceMode;
import com.lanredroidsmith.githubsearch.ui.user.viewmodel.UserListItemViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lanre on 11/8/17.
 */

public class UserAdapter extends ChoiceCapableAdapter<ViewHolder> {
    private List<GitHubUser> mUsers;
    private Context mContext;
    private UserListItemViewModel.OnUserInteractionListener mListener;
    // flag to know if ProgressBar that shows loading status has been added
    private boolean mIsLoading;

    private static final int LOADING = 0;
    private static final int ITEM = 1;

    public UserAdapter(List<GitHubUser> users, UserListItemViewModel.OnUserInteractionListener listener,
                       Context context, RecyclerView rv) {
        super(rv);
        mUsers = users;
        mListener = listener;
        mContext = context;
        mRecyclerView = rv;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (ITEM == viewType) {
            UserItemBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(mContext),
                    R.layout.user_item, parent, false);
            return new BindingHolder(binding);
        } else {
             return new LoadingHolder( LayoutInflater.from(mContext)
                                             .inflate(R.layout.loading_item, parent, false) );
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if ( !mIsLoading || position != mUsers.size() - 1 ) {
            UserItemBinding binding = ((BindingHolder)holder).binding;
            binding.setUvm(new UserListItemViewModel(mUsers.get(position), mListener, mContext));
            if (mChoiceMode != null) {
                binding.userCard.setActivated(isChecked(position));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mUsers != null ? mUsers.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mUsers.size() - 1 && mIsLoading) ? LOADING : ITEM;
    }

    public void addLoading() {
        /*
        *  without the post(Runnable), you get IllegalStateException:
        *  Cannot call this method in a scroll callback.
        * */
        mIsLoading = true;
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mUsers.add(new GitHubUser());
                notifyItemInserted(mUsers.size() - 1);
            }
        });
    }

    public void removeLoading() {
        int position = mUsers.size() - 1;
        mUsers.remove(position);
        notifyItemRemoved(position);
        mIsLoading = false;
    }

    public void setIsLoading(boolean isLoading) {
        mIsLoading = isLoading;
    }

    public void addMoreUsers(List<GitHubUser> users) {
        int currentSize = mUsers.size();
        mUsers.addAll(users);
        notifyItemRangeInserted(currentSize, users.size());
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
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // this is to clear selection from a reused view that was selected
        if (mChoiceMode != null && mChoiceMode instanceof SingleChoiceMode) {
            int position = holder.getAdapterPosition();
            if ( !mIsLoading || position != mUsers.size() - 1 ) {
                if (position != mChoiceMode.getCheckedPosition()) {
                    ((BindingHolder)holder).setChecked(false);
                }
            }
        }
    }

    public void setData(ArrayList<GitHubUser> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    private class BindingHolder extends ViewHolder {
         UserItemBinding binding;

         BindingHolder(UserItemBinding binding) {
            super(binding.userCard);
            this.binding = binding;
         }

        void setChecked(boolean isChecked) {
            binding.userCard.setActivated(isChecked);
        }
    }

    private class LoadingHolder extends ViewHolder {
        LoadingHolder(View itemView) {
            super(itemView);
        }
    }
}
