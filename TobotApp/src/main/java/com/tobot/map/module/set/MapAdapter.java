package com.tobot.map.module.set;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;

/**
 * @author houdeming
 * @date 2018/8/16
 */
public class MapAdapter extends BaseRecyclerAdapter<String> {
    private String mMap;
    private OnMapListener<String> mListener;

    public MapAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final String data, final int position) {
        TextView tvName = (TextView) viewHolder.getView(R.id.tv_map_name);
        Button btnLoad = (Button) viewHolder.getView(R.id.btn_map_load);
        Button btnDelete = (Button) viewHolder.getView(R.id.btn_map_delete);

        if (!TextUtils.isEmpty(data)) {
            // 如果是当前地图的话，就不再提示切换了
            if (TextUtils.equals(data, mMap)) {
                tvName.setText(mContext.getString(R.string.tv_bind_now, data));
                tvName.setSelected(true);
            } else {
                tvName.setText(data);
                tvName.setSelected(false);
            }

            btnLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onMapSwitch(position, data);
                    }
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onMapDelete(position, data);
                    }
                }
            });
        }
    }

    public void setCurrentMap(String name) {
        mMap = name;
    }

    public void setOnMapListener(OnMapListener<String> listener) {
        mListener = listener;
    }

    public interface OnMapListener<T> {
        /**
         * 地图切换
         *
         * @param position
         * @param data
         */
        void onMapSwitch(int position, T data);

        /**
         * 删除地图
         *
         * @param position
         * @param data
         */
        void onMapDelete(int position, T data);
    }
}
