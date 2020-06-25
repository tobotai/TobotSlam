package com.tobot.map.module.task;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

/**
 * @author houdeming
 * @date 2020/3/17
 */
public class GridItemDecoration extends RecyclerView.ItemDecoration {
    private int mHorizontalSpacing, mVerticalSpacing;

    public GridItemDecoration(int horizontalSpacing, int verticalSpacing) {
        mHorizontalSpacing = horizontalSpacing;
        mVerticalSpacing = verticalSpacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        outRect.set(mHorizontalSpacing, mVerticalSpacing, mHorizontalSpacing, mVerticalSpacing);
    }
}