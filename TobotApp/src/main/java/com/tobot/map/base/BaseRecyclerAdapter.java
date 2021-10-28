package com.tobot.map.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author houdeming
 * @date 2018/6/27
 */
@SuppressWarnings("rawtypes")
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter {
    protected List<T> mData = new ArrayList<>();
    protected Context mContext;
    private int mLayoutId;
    private LayoutInflater mLayoutInflater;
    protected OnItemClickListener<T> mOnItemClickListener;

    public BaseRecyclerAdapter(Context context, int itemLayoutId) {
        mContext = context;
        mLayoutId = itemLayoutId;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(mLayoutId, parent, false);
        return new BaseRecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseRecyclerHolder itemHolder = (BaseRecyclerHolder) holder;
        T data = mData != null ? mData.get(position) : null;
        convert(itemHolder, data, position);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    /**
     * 获取子视图
     *
     * @param viewHolder
     * @param data
     * @param position
     */
    public abstract void convert(BaseRecyclerHolder viewHolder, T data, int position);

    public void setData(List<T> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener<T> {
        /**
         * item点击
         *
         * @param position
         * @param data
         */
        void onItemClick(int position, T data);
    }
}
