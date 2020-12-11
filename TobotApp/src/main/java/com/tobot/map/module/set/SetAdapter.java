package com.tobot.map.module.set;

import android.content.Context;

import com.tobot.map.R;

import java.util.List;

import q.rorbin.verticaltablayout.adapter.TabAdapter;
import q.rorbin.verticaltablayout.widget.ITabView;
import q.rorbin.verticaltablayout.widget.QTabView;

/**
 * @author houdeming
 * @date 2019/10/21
 */
public class SetAdapter implements TabAdapter {
    private Context mContext;
    private List<String> mData;

    public SetAdapter(Context context, List<String> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public ITabView.TabBadge getBadge(int position) {
        return null;
    }

    @Override
    public ITabView.TabIcon getIcon(int position) {
        return null;
    }

    @Override
    public ITabView.TabTitle getTitle(int position) {
        return new QTabView.TabTitle.Builder()
                // 设置数据也有设置字体颜色的方法
                .setContent(mData.get(position))
                .setTextColor(mContext.getResources().getColor(R.color.tv_white), mContext.getResources().getColor(R.color.tv_content))
                .build();
    }

    @Override
    public int getBackground(int position) {
        return -1;
    }
}
