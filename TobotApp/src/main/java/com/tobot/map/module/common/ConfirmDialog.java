package com.tobot.map.module.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;

/**
 * @author houdeming
 * @date 2018/8/16
 */
public class ConfirmDialog extends BaseDialog implements View.OnClickListener {
    private TextView tvTips;
    private OnConfirmListener mListener;

    public static ConfirmDialog newInstance(String tips) {
        ConfirmDialog dialog = new ConfirmDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_KEY, tips);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_confirm;
    }

    @Override
    protected void initView(View view) {
        tvTips = view.findViewById(R.id.tv_content);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
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
            tvTips.setText(bundle.getString(DATA_KEY));
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == R.id.btn_confirm) {
            if (mListener != null) {
                mListener.onConfirm();
            }
        }
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        mListener = listener;
    }

    public interface OnConfirmListener {
        /**
         * 确认
         */
        void onConfirm();
    }
}
