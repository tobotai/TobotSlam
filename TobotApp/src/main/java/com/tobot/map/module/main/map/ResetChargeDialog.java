package com.tobot.map.module.main.map;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.SlamManager;

/**
 * @author houdeming
 * @date 2018/8/16
 */
public class ResetChargeDialog extends BaseDialog implements View.OnClickListener {

    public static ResetChargeDialog newInstance() {
        return new ResetChargeDialog();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_reset_charge;
    }

    @Override
    protected void initView(View view) {
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_reset).setOnClickListener(this);
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
        if (id == R.id.btn_cancel) {
            dismiss();
            return;
        }

        if (id == R.id.btn_reset) {
            reset();
        }
    }

    private void reset() {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                boolean isCharge = SlamManager.getInstance().isBatteryCharging();
                if (isCharge) {
                    boolean isSuccess = SlamManager.getInstance().setHomePose(SlamManager.getInstance().getPose());
                    showTips(getString(isSuccess ? R.string.reset_success_tips : R.string.reset_fail_tips), true);
                    return;
                }

                showTips(getString(R.string.reset_not_charge_tips), false);
            }
        });
    }

    private void showTips(final String tips, final boolean isClose) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ToastUtils.getInstance(getActivity()).show(tips);
                if (isClose) {
                    dismiss();
                }
            }
        });
    }
}
