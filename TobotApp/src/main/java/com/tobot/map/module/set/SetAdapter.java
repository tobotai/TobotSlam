package com.tobot.map.module.set;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;
import com.tobot.map.entity.SetBean;

/**
 * @author houdeming
 * @date 2019/10/21
 */
public class SetAdapter extends BaseRecyclerAdapter<SetBean> {
    private int mSelectPosition = 0;

    public SetAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final SetBean data, final int position) {
        TextView tvName = (TextView) viewHolder.getView(R.id.tv_item_name);

        if (data != null) {
            tvName.setText(data.getName());
            boolean isSelected = mSelectPosition == position;
            tvName.setTextSize(mContext.getResources().getDimension(isSelected ? R.dimen.tab_set_tv_select : R.dimen.tab_set_tv_normal));
            tvName.setSelected(isSelected);

            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, data);
                    }
                }
            });
        }
    }

    public void setSelect(int position) {
        if (mSelectPosition == position) {
            return;
        }
        mSelectPosition = position;
        notifyDataSetChanged();
    }
}
