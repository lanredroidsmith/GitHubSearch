package com.lanredroidsmith.githubsearch.ui.common;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by Lanre on 11/9/17.
 */

public abstract class PaginationScrollListener<T extends RecyclerView.LayoutManager> extends
        RecyclerView.OnScrollListener {

    private T mLayoutManager;

    // The minimum amount of items to have below the current scroll position before loading more
    private int mVisibleThreshold;

    protected PaginationScrollListener(T manager, int visibleThreshold) {
        if (visibleThreshold < 0)
            throw new IllegalArgumentException("Threshold cannot be less than zero.");
        if (null == manager)
            throw new IllegalArgumentException("LayoutManager cannot be null.");
        mLayoutManager = manager;
        mVisibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        // 1st check if RecyclerView is scrolling down - to avoid doing unnecessary work
        if (dy > 0) {
            int lastVisibleItemPosition = 0;
            int totalItemCount = mLayoutManager.getItemCount();
            if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) mLayoutManager)
                        .findLastVisibleItemPositions(null);
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
            } else if (mLayoutManager instanceof LinearLayoutManager) {
                // the elseif auto covers GridLayoutManager as it's a subclass of LinearLayoutManager
                lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager)
                        .findLastVisibleItemPosition();
            }
            // We check to see if we have passed the threshold
            // the -1 is VERY IMPORTANT particularly for visible threshold zer0
            if (lastVisibleItemPosition + mVisibleThreshold >= totalItemCount - 1) {
                loadMoreItems();
            }
        }
    }

    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        if (lastVisibleItemPositions.length == 0) return 0;
        int max = lastVisibleItemPositions[0];
        for (int i = 1; i < lastVisibleItemPositions.length; i++) {
            if (lastVisibleItemPositions[i] > max) {
                max = lastVisibleItemPositions[i];
            }
        }
        return max;
    }

    protected abstract void loadMoreItems();
}
