package com.tobot.map.module.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.tobot.map.R;

/**
 * @author houdeming
 * @date 2018/5/16
 */
public class ItemSplitLineDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;
    private int mOrientation;
    private boolean isLastDraw;
    private Paint mPaint;
    private int mSplitLineHeight;

    public ItemSplitLineDecoration(Context context, int orientation, boolean isLastDraw) {
        mOrientation = orientation;
        this.isLastDraw = isLastDraw;
        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(context, R.color.item_split_line));
        mSplitLineHeight = context.getResources().getDimensionPixelSize(R.dimen.item_split_line_height);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent);
            return;
        }

        drawHorizontal(c, parent);
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mSplitLineHeight;
            c.drawRect(left, top, right, bottom, mPaint);
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = child.getRight() + params.rightMargin;
            int right = left + mSplitLineHeight;
            c.drawRect(left, top, right, bottom, mPaint);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            if (isLastDraw) {
                outRect.bottom = mSplitLineHeight;
            }
            return;
        }

        if (isLastDraw) {
            outRect.right = mSplitLineHeight;
        }
    }
}
