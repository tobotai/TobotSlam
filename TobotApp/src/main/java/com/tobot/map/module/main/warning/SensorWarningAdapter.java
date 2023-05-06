package com.tobot.map.module.main.warning;

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
public class SensorWarningAdapter extends BaseRecyclerAdapter<RecordInfo> {

    public SensorWarningAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final RecordInfo data, final int position) {
        TextView tvType = (TextView) viewHolder.getView(R.id.tv_type);
        TextView tvId = (TextView) viewHolder.getView(R.id.tv_id);
        TextView tvCount = (TextView) viewHolder.getView(R.id.tv_count);

        if (data != null) {
            tvType.setText(data.getContent());
            tvId.setText(String.valueOf(data.getCode()));
            tvCount.setText(String.valueOf(data.getCount()));
        }
    }
}
