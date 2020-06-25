package com.tobot.map.module.common;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseV4Dialog;

/**
 * @author houdeming
 * @date 2018/8/16
 */
public class ConfirmDialog extends BaseV4Dialog implements View.OnClickListener {
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            tvTips.setText(bundle.getString(DATA_KEY));
        }
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_confirm:
                dismiss();
                if (mListener != null) {
                    mListener.onConfirm();
                }
                break;
            default:
                break;
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
