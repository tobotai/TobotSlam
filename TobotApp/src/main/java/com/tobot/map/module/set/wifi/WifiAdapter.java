package com.tobot.map.module.set.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;

/**
 * @author houdeming
 * @date 2020/4/28
 */
public class WifiAdapter extends BaseRecyclerAdapter<ScanResult> {

    public WifiAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final ScanResult data, final int position) {
        TextView textView = (TextView) viewHolder.getView(R.id.tv_item_wifi);

        if (data != null) {
            textView.setText(data.SSID);

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
