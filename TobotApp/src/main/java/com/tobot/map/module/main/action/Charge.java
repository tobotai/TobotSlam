package com.tobot.map.module.main.action;

import android.content.Context;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.AbstractPathMonitor;
import com.tobot.map.module.main.MainActivity;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnChargeListener;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
public class Charge extends AbstractPathMonitor implements OnChargeListener {

    public Charge(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
        super(contextWeakReference, activityWeakReference);
    }

    @Override
    public void onChargeSensorTrigger(boolean isEnabled) {
        Logger.i(BaseConstant.TAG, "onChargeSensorTrigger() isEnabled=" + isEnabled);
    }

    @Override
    public void onChargeError() {
        Logger.i(BaseConstant.TAG, "onChargeError()");
        stopMonitor();
        showToast(mContext.getString(R.string.charge_error_tips));
    }

    @Override
    public void onCharging() {
        Logger.i(BaseConstant.TAG, "onCharging()");
        stopMonitor();
        showToast(mContext.getString(R.string.charge_ing_tips));
    }

    @Override
    public void onChargeResult(boolean isChargeSuccess) {
        Logger.i(BaseConstant.TAG, "onChargeResult()=" + isChargeSuccess);
        stopMonitor();
        showToast(mContext.getString(R.string.charge_result, isChargeSuccess));
        if (mActivity != null) {
            mActivity.handleMoveResult(isChargeSuccess);
        }
    }

    @Override
    public void onObstacleTrigger() {
        SlamManager.getInstance().cancelAction();
    }

    @Override
    public void onObstacleDisappear() {
        goCharge();
    }

    public void goCharge() {
        startMonitor();
        SlamManager.getInstance().goHome(this);
    }

    public void stop() {
        stopMonitor();
    }
}
