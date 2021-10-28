package com.tobot.map.module.set.firmware;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;
import com.tobot.map.entity.SetBean;

/**
 * @author houdeming
 * @date 2021/03/19
 */
public class SensorDetectAdapter extends BaseRecyclerAdapter<SetBean> {
    private int mSelectIndex = -1;
    private boolean isSelectAll;

    public SensorDetectAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final SetBean data, final int position) {
        LinearLayout llRoot = (LinearLayout) viewHolder.getView(R.id.ll_root);
        TextView tvName = (TextView) viewHolder.getView(R.id.tv_item_name);

        if (data != null) {
            tvName.setText(data.getName());
            llRoot.setSelected(isSelectAll || mSelectIndex == position);

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

    public void setSelect(int position, boolean isSelectAll) {
        mSelectIndex = position;
        this.isSelectAll = isSelectAll;
        notifyDataSetChanged();
    }
}
