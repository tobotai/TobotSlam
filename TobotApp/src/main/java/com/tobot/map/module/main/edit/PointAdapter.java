package com.tobot.map.module.main.edit;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;
import com.tobot.slam.data.LocationBean;

/**
 * @author houdeming
 * @date 2020/4/28
 */
public class PointAdapter extends BaseRecyclerAdapter<LocationBean> {

    public PointAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final LocationBean data, final int position) {
        TextView textView = (TextView) viewHolder.getView(R.id.tv_item_num);

        if (data != null) {
            textView.setText(data.getLocationNumber());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, data);
                    }
                }
            });
        }
    }
}
