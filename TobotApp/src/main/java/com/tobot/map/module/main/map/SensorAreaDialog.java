package com.tobot.map.module.main.map;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.data.LocationBean;

/**
 * @author houdeming
 * @date 2020/3/14
 */
public class SensorAreaDialog extends BaseDialog implements View.OnClickListener, PoseAdapter.OnPoseListener {
    private TextView tvTips;
    private EditText etStartX, etStartY, etEndX, etEndY;
    private OnSensorAreaListener mOnSensorAreaListener;
    private boolean isEnd;

    public static SensorAreaDialog newInstance(LocationBean bean) {
        SensorAreaDialog dialog = new SensorAreaDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DATA_KEY, bean);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_sensor_area;
    }

    @Override
    protected void initView(View view) {
        tvTips = view.findViewById(R.id.tv_point_tips);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_pose);
        etStartX = view.findViewById(R.id.et_start_x);
        etStartY = view.findViewById(R.id.et_start_y);
        etEndX = view.findViewById(R.id.et_end_x);
        etEndY = view.findViewById(R.id.et_end_y);
        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
        PoseAdapter adapter = new PoseAdapter(getActivity(), R.layout.recycler_item_pose);
        adapter.setOnPoseListener(this);
        recyclerView.setAdapter(adapter);
        adapter.setData(MyDBSource.getInstance(getActivity()).queryLocation());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            LocationBean bean = bundle.getParcelable(DATA_KEY);
            if (bean != null) {
                tvTips.setText(getString(R.string.tv_point_tips, bean.getLocationNumber()));
                float startX = bean.getStartX();
                if (startX != 0) {
                    etStartX.setText(String.valueOf(startX));
                }
                float startY = bean.getStartY();
                if (startY != 0) {
                    etStartY.setText(String.valueOf(startY));
                }
                float endX = bean.getEndX();
                if (endX != 0) {
                    etEndX.setText(String.valueOf(endX));
                }
                float endY = bean.getEndY();
                if (endY != 0) {
                    etEndY.setText(String.valueOf(endY));
                }
            }
        }
    }

    @Override
    protected double getScreenWidthPercentage() {
        return 1;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            confirm();
        }
    }

    @Override
    public void onPose(float x, float y) {
        if (isEnd) {
            isEnd = false;
            etEndX.setText(String.valueOf(x));
            etEndY.setText(String.valueOf(y));
            return;
        }
        isEnd = true;
        etStartX.setText(String.valueOf(x));
        etStartY.setText(String.valueOf(y));
    }

    private void confirm() {
        String startX = etStartX.getText().toString();
        String startY = etStartY.getText().toString();
        String endX = etEndX.getText().toString();
        String endY = etEndY.getText().toString();
        if (TextUtils.isEmpty(startX)) {
            ToastUtils.getInstance(getActivity()).show(getString(R.string.start_x_empty_tips));
            return;
        }
        if (TextUtils.isEmpty(startY)) {
            ToastUtils.getInstance(getActivity()).show(getString(R.string.start_y_empty_tips));
            return;
        }
        if (TextUtils.isEmpty(endX)) {
            ToastUtils.getInstance(getActivity()).show(getString(R.string.end_x_empty_tips));
            return;
        }
        if (TextUtils.isEmpty(endY)) {
            ToastUtils.getInstance(getActivity()).show(getString(R.string.end_y_empty_tips));
            return;
        }

        dismiss();
        if (mOnSensorAreaListener != null) {
            mOnSensorAreaListener.onSensorArea(Float.parseFloat(startX), Float.parseFloat(startY), Float.parseFloat(endX), Float.parseFloat(endY));
        }
    }

    public void setOnSensorAreaListener(OnSensorAreaListener listener) {
        mOnSensorAreaListener = listener;
    }

    public interface OnSensorAreaListener {
        /**
         * 传感器区域回调
         *
         * @param startX
         * @param startY
         * @param endX
         * @param endY
         */
        void onSensorArea(float startX, float startY, float endX, float endY);
    }
}
