package com.tobot.map.module.main.map;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;
import com.tobot.slam.data.LocationBean;

/**
 * @author houdeming
 * @date 2019/10/22
 */
public class LocationAdapter extends BaseRecyclerAdapter<LocationBean> {
    private OnLocationListener mListener;

    public LocationAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final LocationBean data, final int position) {
        Button btnLocation = (Button) viewHolder.getView(R.id.btn_location);
        TextView tvName = (TextView) viewHolder.getView(R.id.tv_location_name);
        Button btnEdit = (Button) viewHolder.getView(R.id.btn_edit);
        Button btnDelete = (Button) viewHolder.getView(R.id.btn_delete);

        if (data != null) {
            btnLocation.setText(data.getLocationNumber());
            String chinaName = data.getLocationNameChina();
            String englishName = data.getLocationNameEnglish();
            if (!TextUtils.isEmpty(chinaName) && !TextUtils.isEmpty(englishName)) {
                tvName.setText(chinaName.concat("(").concat(englishName).concat(")"));
            } else if (!TextUtils.isEmpty(chinaName)) {
                tvName.setText(chinaName);
            } else if (!TextUtils.isEmpty(englishName)) {
                tvName.setText(englishName);
            } else {
                tvName.setText("");
            }

            btnLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onMoveTo(data);
                    }
                }
            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onEditLocation(data, position);
                    }
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDeleteLocation(data, position);
                    }
                }
            });
        }
    }

    public void setOnLocationListener(OnLocationListener listener) {
        mListener = listener;
    }

    public interface OnLocationListener {
        /**
         * 导航到位置
         *
         * @param data
         */
        void onMoveTo(LocationBean data);

        /**
         * 编辑位置
         *
         * @param data
         * @param position
         */
        void onEditLocation(LocationBean data, int position);

        /**
         * 删除位置
         *
         * @param data
         * @param position
         */
        void onDeleteLocation(LocationBean data, int position);
    }
}
