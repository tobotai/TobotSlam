package com.tobot.map.module.common;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

/**
 * @author houdeming
 * @date 2020/3/17
 */
public class GridItemDecoration extends RecyclerView.ItemDecoration {
    private final int mHorizontalSpacing;
    private final int mVerticalSpacing;

    public GridItemDecoration(int horizontalSpacing, int verticalSpacing) {
        mHorizontalSpacing = horizontalSpacing;
        mVerticalSpacing = verticalSpacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull State state) {
        outRect.set(mHorizontalSpacing, mVerticalSpacing, mHorizontalSpacing, mVerticalSpacing);
    }
}