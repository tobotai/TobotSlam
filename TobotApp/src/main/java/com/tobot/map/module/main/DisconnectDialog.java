package com.tobot.map.module.main;

import android.view.View;

import com.tobot.map.R;
import com.tobot.map.base.BaseV4Dialog;

/**
 * @author houdeming
 * @date 2018/8/16
 */
public class DisconnectDialog extends BaseV4Dialog implements View.OnClickListener {
    private OnOperateListener mListener;

    public static DisconnectDialog newInstance() {
        return new DisconnectDialog();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_disconnect;
    }

    @Override
    protected void initView(View view) {
        view.findViewById(R.id.btn_reconnect).setOnClickListener(this);
        view.findViewById(R.id.btn_exit).setOnClickListener(this);
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
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_reconnect) {
            callBackOperate(OnOperateListener.OPERATE_RECONNECT);
            return;
        }

        if (id == R.id.btn_exit) {
            callBackOperate(OnOperateListener.OPERATE_EXIT);
        }
    }

    public void setOnOperateListener(OnOperateListener listener) {
        mListener = listener;
    }

    private void callBackOperate(int type) {
        dismiss();
        if (mListener != null) {
            mListener.onOperate(type);
        }
    }

    public interface OnOperateListener {
        int OPERATE_RECONNECT = 0;
        int OPERATE_EXIT = 1;

        /**
         * 操作
         *
         * @param type
         */
        void onOperate(int type);
    }
}
