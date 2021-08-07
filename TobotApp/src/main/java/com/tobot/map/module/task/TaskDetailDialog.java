package com.tobot.map.module.task;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.data.LocationBean;

import java.util.List;

/**
 * @author houdeming
 * @date 2019/10/24
 */
public class TaskDetailDialog extends BaseDialog implements View.OnClickListener {
    private TextView tvTitle, tvContent;
    private String mTitle;
    private OnDeleteListener mOnDeleteListener;

    public static TaskDetailDialog newInstance(String titleTips) {
        TaskDetailDialog dialog = new TaskDetailDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_KEY, titleTips);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_task_detail;
    }

    @Override
    protected void initView(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        tvContent = view.findViewById(R.id.tv_content);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_delete).setOnClickListener(this);
    }

    @Override
    protected boolean isCanCancelByBack() {
        return true;
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTitle = bundle.getString(DATA_KEY);
            tvTitle.setText(mTitle);
            List<LocationBean> locationBeanList = MyDBSource.getInstance(getActivity()).queryRouteDetailList(mTitle);
            tvContent.setText(getString(R.string.tv_task_point_tips, DataHelper.getInstance().getTaskDetailTips(getActivity(), locationBeanList)));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_cancel) {
            dismiss();
            return;
        }

        if (id == R.id.btn_delete) {
            MyDBSource.getInstance(getActivity()).deleteRoute(mTitle);
            MyDBSource.getInstance(getActivity()).deleteRouteDetail(mTitle);
            ToastUtils.getInstance(getActivity()).show(R.string.delete_success);
            dismiss();
            if (mOnDeleteListener != null) {
                mOnDeleteListener.onDelete();
            }
        }
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        mOnDeleteListener = listener;
    }

    public interface OnDeleteListener {
        /**
         * 删除
         */
        void onDelete();
    }
}
