package com.lanredroidsmith.githubsearch.ui.common;

import android.os.Bundle;

/**
 * Created by Lanre on 11/19/17.
 */

public class SingleChoiceMode implements ChoiceMode {
    private static final String STATE_CHECKED = "checkedPosition";
    private int checkedPosition = -1;

    @Override
    public boolean isSingleChoice() {
        return(true);
    }

    @Override
    public int getCheckedPosition() {
        return(checkedPosition);
    }

    @Override
    public void setChecked(int position, boolean isChecked) {
        if (isChecked) {
            checkedPosition = position;
        } else if (isChecked(position)) {
            /*
            *  useful if we're also unchecking on a 2nd click.
            *  We don't need it in this project though
            */
            checkedPosition = -1;
        }
    }

    public void clearSelection() {
        checkedPosition = -1;
    }

    @Override
    public boolean isChecked(int position) {
        return(checkedPosition == position);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt(STATE_CHECKED, checkedPosition);
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        checkedPosition = state.getInt(STATE_CHECKED, -1);
    }
}