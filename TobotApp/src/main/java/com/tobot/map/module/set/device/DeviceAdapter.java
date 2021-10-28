package com.tobot.map.module.set.device;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;

/**
 * @author houdeming
 * @date 2020/4/28
 */
public class DeviceAdapter extends BaseRecyclerAdapter<DeviceBean> {

    public DeviceAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, DeviceBean data, int position) {
        TextView tvName = (TextView) viewHolder.getView(R.id.tv_name);
        TextView tvContent = (TextView) viewHolder.getView(R.id.tv_content);

        if (data != null) {
            tvName.setText(data.getName());
            String content = data.getContent();
            // 默认未知
            tvContent.setText(TextUtils.isEmpty(content) ? mContext.getString(R.string.unknown) : content);
        }
    }
}
