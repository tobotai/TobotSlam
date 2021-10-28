package com.tobot.map.module.common;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author houdeming
 * @date 2020/12/22
 */
public class FlowLayoutManager extends RecyclerView.LayoutManager {
    private int left, top;
    private int usedMaxWidth;
    private int verticalScrollOffset = 0;
    private int totalHeight = 0;
    private Row row = new Row();
    private List<Row> lineRows = new ArrayList<>();
    /**
     * 保存所有的Item的上下左右的偏移量信息
     */
    private SparseArray<Rect> allItemFrames = new SparseArray<>();

    public FlowLayoutManager() {
        // 设置主动测量规则，适应recyclerView高度为wrap_content
        setAutoMeasureEnabled(true);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 该方法主要用来获取每一个item在屏幕上占据的位置
     *
     * @param recycler
     * @param state
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        totalHeight = 0;
        int cuLineTop = top;
        // 当前行使用的宽度
        int cuLineWidth = 0;
        int itemLeft;
        int itemTop;
        int maxHeightItem = 0;
        row = new Row();
        lineRows.clear();
        allItemFrames.clear();
        removeAllViews();
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            verticalScrollOffset = 0;
            return;
        }

        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        // onLayoutChildren方法在RecyclerView 初始化时 会执行两遍
        detachAndScrapAttachedViews(recycler);
        if (getChildCount() == 0) {
            int width = getWidth();
            int height = getHeight();
            left = getPaddingLeft();
            int right = getPaddingRight();
            top = getPaddingTop();
            usedMaxWidth = width - left - right;
        }

        for (int i = 0, itemCount = getItemCount(); i < itemCount; i++) {
            View childAt = recycler.getViewForPosition(i);
            if (View.GONE == childAt.getVisibility()) {
                continue;
            }

            measureChildWithMargins(childAt, 0, 0);
            int childWidth = getDecoratedMeasuredWidth(childAt);
            int childHeight = getDecoratedMeasuredHeight(childAt);
            // 如果加上当前的item还小于最大的宽度的话就增加，否则就换行
            if (cuLineWidth + childWidth <= usedMaxWidth) {
                itemLeft = left + cuLineWidth;
                itemTop = cuLineTop;
                Rect frame = allItemFrames.get(i);
                if (frame == null) {
                    frame = new Rect();
                }
                frame.set(itemLeft, itemTop, itemLeft + childWidth, itemTop + childHeight);
                allItemFrames.put(i, frame);
                cuLineWidth += childWidth;
                maxHeightItem = Math.max(maxHeightItem, childHeight);
                row.addViews(new Item(childHeight, childAt, frame));
                row.setCuTop(cuLineTop);
                row.setMaxHeight(maxHeightItem);
            } else {
                // 换行
                formatAboveRow();
                cuLineTop += maxHeightItem;
                totalHeight += maxHeightItem;
                itemTop = cuLineTop;
                itemLeft = left;
                Rect frame = allItemFrames.get(i);
                if (frame == null) {
                    frame = new Rect();
                }
                frame.set(itemLeft, itemTop, itemLeft + childWidth, itemTop + childHeight);
                allItemFrames.put(i, frame);
                cuLineWidth = childWidth;
                maxHeightItem = childHeight;
                row.addViews(new Item(childHeight, childAt, frame));
                row.setCuTop(cuLineTop);
                row.setMaxHeight(maxHeightItem);
            }

            // 不要忘了最后一行进行刷新下布局
            if (i == itemCount - 1) {
                formatAboveRow();
                totalHeight += maxHeightItem;
            }
        }

        totalHeight = Math.max(totalHeight, getVerticalSpace());
        fillLayout(recycler, state);
    }

    /**
     * 竖直方向需要滑动的条件
     *
     * @return
     */
    @Override
    public boolean canScrollVertically() {
        return true;
    }

    /**
     * 监听竖直方向滑动的偏移量
     *
     * @param dy
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 实际要滑动的距离
        int travel = dy;
        // 如果滑动到最顶部
        if (verticalScrollOffset + dy < 0) {
            // 限制滑动到顶部之后，不让继续向上滑动了
            travel = -verticalScrollOffset;
        } else if (verticalScrollOffset + dy > totalHeight - getVerticalSpace()) {
            // 如果滑动到最底部
            travel = totalHeight - getVerticalSpace() - verticalScrollOffset;
        }
        // 将竖直方向的偏移量+travel
        verticalScrollOffset += travel;
        // 平移容器内的item
        offsetChildrenVertical(-travel);
        fillLayout(recycler, state);
        return travel;
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    private int getTotalHeight() {
        return totalHeight;
    }

    /**
     * 对出现在屏幕上的item进行展示，超出屏幕的item回收到缓存中
     *
     * @param recycler
     * @param state
     */
    private void fillLayout(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 跳过preLayout，preLayout主要用于支持动画
        if (state.isPreLayout() || getItemCount() == 0) {
            return;
        }

        // 当前scroll offset状态下的显示区域
        Rect displayFrame = new Rect(getPaddingLeft(), getPaddingTop() + verticalScrollOffset,
                getWidth() - getPaddingRight(), verticalScrollOffset + (getHeight() - getPaddingBottom()));
        // 对所有的行信息进行遍历
        for (int j = 0, rows = lineRows.size(); j < rows; j++) {
            Row row = lineRows.get(j);
            float lineTop = row.cuTop;
            float lineBottom = lineTop + row.maxHeight;
            // 如果该行在屏幕中，进行放置item
//            if (lineTop < displayFrame.bottom && displayFrame.top < lineBottom) {
            List<Item> views = row.views;
            for (int i = 0, viewSize = views.size(); i < viewSize; i++) {
                View scrap = views.get(i).view;
                measureChildWithMargins(scrap, 0, 0);
                addView(scrap);
                Rect frame = views.get(i).rect;
                // 将这个item布局出来
                layoutDecoratedWithMargins(scrap,
                        frame.left,
                        frame.top - verticalScrollOffset,
                        frame.right,
                        frame.bottom - verticalScrollOffset);
            }
//            } else {
//                // 将不在屏幕中的item放到缓存中
//                List<Item> views = row.views;
//                for (int i = 0; i < views.size(); i++) {
//                    View scrap = views.get(i).view;
//                    removeAndRecycleView(scrap, recycler);
//                }
//            }
        }
    }

    /**
     * 计算每一行没有居中的viewgroup，让居中显示
     */
    private void formatAboveRow() {
        List<Item> views = row.views;
        for (int i = 0, viewSize = views.size(); i < viewSize; i++) {
            Item item = views.get(i);
            View view = item.view;
            int position = getPosition(view);
            // 如果该item的位置不在该行中间位置的话，进行重新放置
            if (allItemFrames.get(position).top < row.cuTop + (row.maxHeight - views.get(i).useHeight) / 2) {
                Rect frame = allItemFrames.get(position);
                if (frame == null) {
                    frame = new Rect();
                }
                frame.set(allItemFrames.get(position).left, (int) (row.cuTop + (row.maxHeight - views.get(i).useHeight) / 2),
                        allItemFrames.get(position).right, (int) (row.cuTop + (row.maxHeight - views.get(i).useHeight) / 2 + getDecoratedMeasuredHeight(view)));
                allItemFrames.put(position, frame);
                item.setRect(frame);
                views.set(i, item);
            }
        }

        row.views = views;
        lineRows.add(row);
        row = new Row();
    }

    /**
     * 每个item的定义
     */
    private static class Item {
        private int useHeight;
        private View view;
        private Rect rect;

        private void setRect(Rect rect) {
            this.rect = rect;
        }

        private Item(int useHeight, View view, Rect rect) {
            this.useHeight = useHeight;
            this.view = view;
            this.rect = rect;
        }
    }

    /**
     * 行信息的定义
     */
    private static class Row {
        /**
         * 每一行的头部坐标
         */
        private float cuTop;
        /**
         * 每一行需要占据的最大高度
         */
        private float maxHeight;
        /**
         * 每一行存储的item
         */
        private List<Item> views = new ArrayList<>();

        private void addViews(Item view) {
            views.add(view);
        }

        private void setCuTop(float cuTop) {
            this.cuTop = cuTop;
        }

        private void setMaxHeight(float maxHeight) {
            this.maxHeight = maxHeight;
        }
    }
}
