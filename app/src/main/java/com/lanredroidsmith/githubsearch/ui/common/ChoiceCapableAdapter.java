package com.lanredroidsmith.githubsearch.ui.common;

/**
 * Created by Lanre on 11/19/17.
 */

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public abstract class ChoiceCapableAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {
    protected ChoiceMode mChoiceMode;
    protected RecyclerView mRecyclerView;

    public ChoiceCapableAdapter(RecyclerView rv) {
        super();
        mRecyclerView = rv;
    }

    public boolean isChecked(int position) {
        return(mChoiceMode.isChecked(position));
    }

    public void onSaveInstanceState(Bundle state) {
        mChoiceMode.onSaveInstanceState(state);
    }

    public void onRestoreInstanceState(Bundle state) {
        mChoiceMode.onRestoreInstanceState(state);
    }

    public abstract void setItemChecked(int position, boolean isChecked);

    public void setChoiceMode(ChoiceMode cm) { mChoiceMode = cm; }

    public ChoiceMode getChoiceMode() { return mChoiceMode; }

    @Override
    public void onViewAttachedToWindow(T holder) {
        super.onViewAttachedToWindow(holder);
    }
}