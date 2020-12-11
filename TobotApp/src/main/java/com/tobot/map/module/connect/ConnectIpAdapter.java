package com.tobot.map.module.connect;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;

/**
 * @author houdeming
 * @date 2019/10/19
 */
public class ConnectIpAdapter extends BaseRecyclerAdapter<String> {

    public ConnectIpAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final String data, final int position) {
        TextView tvIp = (TextView) viewHolder.getView(R.id.tv_item_ip);

        if (!TextUtils.isEmpty(data)) {
            tvIp.setText(data);
            
            tvIp.setOnClickListener(new View.OnClickListener() {
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
