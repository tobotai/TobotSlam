package com.tobot.map.module.main.action;

import android.content.Context;
import android.view.View;

import com.tobot.map.R;
import com.tobot.map.module.common.BasePopupWindow;

import java.lang.ref.WeakReference;

/**
 * 动作
 *
 * @author houdeming
 * @date 2020/3/15
 */
public class ActionPopupWindow extends BasePopupWindow {
    private OnChargeListener mOnChargeListener;

    public ActionPopupWindow(Context context, OnChargeListener listener) {
        super(context);
        mOnChargeListener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.popup_action;
    }

    @Override
    public void initView(View view) {
        RockerView rockerView = view.findViewById(R.id.view_rocker);
        view.findViewById(R.id.tv_go_charge).setOnClickListener(this);
        new RockerControlHelper(new WeakReference<>(rockerView));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_go_charge) {
            dismiss();
            if (mOnChargeListener != null) {
                mOnChargeListener.onCharge();
            }
        }
    }

    public interface OnChargeListener {
        /**
         * 充电
         */
        void onCharge();
    }
}
