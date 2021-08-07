package com.tobot.map.module.task;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;
import com.tobot.slam.data.LocationBean;

import java.util.List;

/**
 * @author houdeming
 * @date 2020/3/17
 */
public class TaskPointSelectAdapter extends BaseRecyclerAdapter<LocationBean> {
    private List<LocationBean> mSelectList;

    public TaskPointSelectAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final LocationBean data, final int position) {
        LinearLayout llRoot = (LinearLayout) viewHolder.getView(R.id.ll_root);
        TextView tvNum = (TextView) viewHolder.getView(R.id.tv_item_num);
        TextView tvName = (TextView) viewHolder.getView(R.id.tv_item_name);

        if (data != null) {
            llRoot.setSelected(false);
            tvNum.setText(data.getLocationNumber());
            String name = data.getLocationNameChina();
            if (!TextUtils.isEmpty(name)) {
                tvName.setVisibility(View.VISIBLE);
                tvName.setText(name);
            } else {
                tvName.setVisibility(View.GONE);
            }

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
        }
    }

    public void setSelectData(List<LocationBean> data) {
        mSelectList = data;
        notifyDataSetChanged();
    }
}
