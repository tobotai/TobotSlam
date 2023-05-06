package com.tobot.map.module.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * @author houdeming
 * @date 2020/6/28
 */
public abstract class AbstractLoadMore extends RecyclerView.OnScrollListener {
    private int mLastVisibleItem, mItemCount;

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        // 在onScrolled()方法之后执行
        // 只有RecyclerView的状态是空闲时，同时是最后一个可见的item时才加载
        if (newState == RecyclerView.SCROLL_STATE_IDLE && mLastVisibleItem + 1 == mItemCount) {
            if (mLastVisibleItem >= 0) {
                onLoadMore(mLastVisibleItem);
            }
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        // 默认会执行这个方法
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            // 获取最后一个可见的item，从0开始
            mLastVisibleItem = layoutManager.findLastVisibleItemPosition();
            // 获取当前总item个数
            mItemCount = layoutManager.getItemCount();
        }
    }

    /**
     * 加载更多
     *
     * @param lastItem
     */
    protected abstract void onLoadMore(int lastItem);
}
