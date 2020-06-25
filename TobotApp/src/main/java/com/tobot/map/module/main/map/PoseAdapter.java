package com.tobot.map.module.main.map;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;
import com.tobot.slam.data.LocationBean;

/**
 * @author houdeming
 * @date 2020/3/14
 */
public class PoseAdapter extends BaseRecyclerAdapter<LocationBean> {
    private OnPoseListener mOnPoseListener;

    public PoseAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    public void setOnPoseListener(OnPoseListener listener) {
        mOnPoseListener = listener;
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final LocationBean data, int position) {
        LinearLayout llPose = (LinearLayout) viewHolder.getView(R.id.ll_pose);
        TextView tvNum = (TextView) viewHolder.getView(R.id.tv_location_num);
        TextView tvPose = (TextView) viewHolder.getView(R.id.tv_location_pose);
        if (data != null) {
            tvNum.setText(data.getLocationNumber());
            tvPose.setText(mContext.getString(R.string.tv_pose_tips, data.getX(), data.getY()));

            llPose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnPoseListener != null) {
                        mOnPoseListener.onPose(data.getX(), data.getY());
                    }
                }
            });
        }
    }

    public interface OnPoseListener {
        /**
         * 位置
         *
         * @param x
         * @param y
         */
        void onPose(float x, float y);
    }
}
