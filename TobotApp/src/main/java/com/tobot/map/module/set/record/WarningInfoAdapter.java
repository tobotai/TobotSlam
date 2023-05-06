package com.tobot.map.module.set.record;

import android.content.Context;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;
import com.tobot.map.entity.RecordInfo;

/**
 * @author houdeming
 * @date 2021/4/17
 */
public class WarningInfoAdapter extends BaseRecyclerAdapter<RecordInfo> {

    public WarningInfoAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final RecordInfo data, final int position) {
        TextView tvTime = (TextView) viewHolder.getView(R.id.tv_time);
        TextView tvDescribe = (TextView) viewHolder.getView(R.id.tv_describe);

        if (data != null) {
            tvTime.setText(data.getTime());
            tvDescribe.setText(data.getContent());
        }
    }
}
