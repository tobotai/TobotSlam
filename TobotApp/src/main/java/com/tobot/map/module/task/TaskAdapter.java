package com.tobot.map.module.task;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;
import com.tobot.map.entity.RouteBean;

import java.util.List;

/**
 * @author houdeming
 * @date 2020/3/17
 */
public class TaskAdapter extends BaseRecyclerAdapter<RouteBean> {
    private List<RouteBean> mSelectList;
    private OnItemLongClickListener mOnItemLongClickListener;

    public TaskAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    public void setSelectData(List<RouteBean> data) {
        mSelectList = data;
        notifyDataSetChanged();
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final RouteBean data, final int position) {
        LinearLayout llRoot = (LinearLayout) viewHolder.getView(R.id.ll_root);
        TextView tvName = (TextView) viewHolder.getView(R.id.tv_item_name);

        if (data != null) {
            llRoot.setSelected(false);
            tvName.setText(data.getRouteName());
            if (mSelectList != null && !mSelectList.isEmpty()) {
                if (mSelectList.contains(data)) {
                    // 设置选中的背景
                    llRoot.setSelected(true);
                }
            }

            llRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, data);
                    }
                }
            });

            llRoot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemLongClickListener != null) {
                        mOnItemLongClickListener.onItemLongClick(position, data);
                    }
                    return true;
                }
            });
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public interface OnItemLongClickListener {
        /**
         * item长按点击
         *
         * @param position
         * @param data
         */
        void onItemLongClick(int position, RouteBean data);
    }
}
