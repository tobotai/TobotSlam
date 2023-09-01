package com.tobot.map.module.main.map;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnResultListener;

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
        SlamManager.getInstance().setHomePoseAsync(DataHelper.getInstance().getChassisRadius(getActivity()), new OnResultListener<Boolean>() {
            @Override
            public void onResult(Boolean data) {
                if (!data) {
                    boolean isCharge = SlamManager.getInstance().isBatteryCharging();
                    // 不在充电桩上的提示
                    if (!isCharge) {
                        showTips(getString(R.string.reset_not_charge_tips), false);
                        return;
                    }
                }

                showTips(getString(data ? R.string.reset_success : R.string.reset_fail), true);
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
