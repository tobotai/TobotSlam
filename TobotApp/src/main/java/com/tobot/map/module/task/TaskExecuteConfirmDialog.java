package com.tobot.map.module.task;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.main.DataHelper;
import com.tobot.slam.data.LocationBean;
import com.tobot.wheelview.OnItemSelectedListener;
import com.tobot.wheelview.WheelView;
import com.tobot.wheelview.adapter.NumberWheelAdapter;

import java.util.List;

/**
 * @author houdeming
 * @date 2019/10/24
 */
public class TaskExecuteConfirmDialog extends BaseDialog implements View.OnClickListener, OnItemSelectedListener {
    private static final int MAX = 200;
    private static final float LINE_SPACE = 1.5f;
    private static final int ITEM_VISIBLE_COUNT = 5;
    private TextView tvTitle, tvTaskPoint, tvChargeSwitch, tvLoopCount, tvLoop;
    /**
     * 默认执行一次
     */
    private int mLoopCount = 1;
    private OnExecuteListener mOnExecuteListener;
    private List<LocationBean> mLocationList;

    public static TaskExecuteConfirmDialog newInstance(String task) {
        TaskExecuteConfirmDialog dialog = new TaskExecuteConfirmDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_KEY, task);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_task_execute_confirm;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initView(View view) {
        tvTitle = view.findViewById(R.id.tv_head);
        tvTaskPoint = view.findViewById(R.id.tv_content);
        tvChargeSwitch = view.findViewById(R.id.tv_charge_switch);
        tvLoopCount = view.findViewById(R.id.tv_implement_count);
        WheelView wheelView = view.findViewById(R.id.wheel_view_count);
        tvLoop = view.findViewById(R.id.tv_loop_implement);
        tvLoopCount.setText(getString(R.string.tv_implement_count, mLoopCount));
        tvChargeSwitch.setOnClickListener(this);
        tvLoop.setOnClickListener(this);
        view.findViewById(R.id.tv_back).setOnClickListener(this);
        view.findViewById(R.id.btn_start_implement).setOnClickListener(this);
        wheelView.setDividerType(WheelView.DividerType.WRAP);
        wheelView.setLineSpacingMultiplier(LINE_SPACE);
        wheelView.setItemsVisibleCount(ITEM_VISIBLE_COUNT);
        int item = mLoopCount >= 1 ? mLoopCount - 1 : 0;
        wheelView.setCurrentItem(item);
        wheelView.setOnItemSelectedListener(this);
        NumberWheelAdapter adapter = new NumberWheelAdapter(1, MAX);
        wheelView.setAdapter(adapter);
        // 解决wheelView的滑动冲突
        wheelView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 屏蔽父类的事件
                wheelView.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    @Override
    protected boolean isCanCancelByBack() {
        return true;
    }

    @Override
    protected double getScreenWidthPercentage() {
        return 1;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString(DATA_KEY);
            tvTitle.setText(name);
            mLocationList = MyDBSource.getInstance(getActivity()).queryRouteDetailList(name);
            tvTaskPoint.setText(getString(R.string.tv_task_point_tips, DataHelper.getInstance().getTaskDetailTips(getActivity(), mLocationList)));
        }
    }

    @Override
    public void onItemSelected(int index) {
        // index从0开始
        mLoopCount = index + 1;
        tvLoopCount.setText(getString(R.string.tv_implement_count, mLoopCount));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                dismiss();
                break;
            case R.id.tv_loop_implement:
                tvLoop.setSelected(!tvLoop.isSelected());
                break;
            case R.id.btn_start_implement:
                dismiss();
                if (mOnExecuteListener != null) {
                    mOnExecuteListener.onExecute(mLocationList, tvChargeSwitch.isSelected(), tvLoop.isSelected() ? BaseConstant.LOOP_INFINITE : mLoopCount);
                }
                break;
            case R.id.tv_charge_switch:
                tvChargeSwitch.setSelected(!tvChargeSwitch.isSelected());
                break;
            default:
                break;
        }
    }

    public void setOnExecuteListener(OnExecuteListener listener) {
        mOnExecuteListener = listener;
    }

    public interface OnExecuteListener {
        /**
         * 执行
         *
         * @param locationBeans
         * @param isAddCharge
         * @param loopCount
         */
        void onExecute(List<LocationBean> locationBeans, boolean isAddCharge, int loopCount);
    }
}
