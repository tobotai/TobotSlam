package com.tobot.map.module.upgrade;

import android.view.View;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;

/**
 * @author houdeming
 * @date 2018/7/19
 */
public class UpgradeTipsDialog extends BaseDialog implements View.OnClickListener {
    private OnUpgradeListener mListener;

    public static UpgradeTipsDialog newInstance() {
        return new UpgradeTipsDialog();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_upgrade_tips;
    }

    @Override
    protected void initView(View view) {
        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    protected boolean isCanCancelByBack() {
        return false;
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    @Override
    public void onClick(View v) {
        boolean isUpgrade = v.getId() == R.id.btn_confirm;
        dismiss();
        if (mListener != null) {
            mListener.onUpgrade(isUpgrade);
        }
    }

    public void setOnUpgradeListener(OnUpgradeListener listener) {
        mListener = listener;
    }

    public interface OnUpgradeListener {
        /**
         * 升级
         *
         * @param isUpgrade
         */
        void onUpgrade(boolean isUpgrade);
    }
}
