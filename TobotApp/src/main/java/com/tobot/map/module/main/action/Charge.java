package com.tobot.map.module.main.action;

import android.content.Context;
import android.os.Handler;

import com.tobot.map.R;
import com.tobot.map.module.main.AbsPathMonitor;
import com.tobot.map.util.LogUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnChargeListener;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
public class Charge extends AbsPathMonitor implements OnChargeListener {

    public Charge(WeakReference<Context> contextWeakReference, WeakReference<Handler> handlerWeakReference) {
        super(contextWeakReference, handlerWeakReference);
    }

    @Override
    public void onChargeSensorTrigger(boolean isEnabled) {
        LogUtils.i("onChargeSensorTrigger() isEnabled=" + isEnabled);
    }

    @Override
    public void onChargeError() {
        LogUtils.i("onChargeError()");
        stopMonitor();
        showToast(mContext.getString(R.string.charge_error_tips));
    }

    @Override
    public void onCharging() {
        LogUtils.i("onCharging()");
        stopMonitor();
        showToast(mContext.getString(R.string.charge_ing_tips));
    }

    @Override
    public void onChargeResult(boolean isChargeSuccess) {
        LogUtils.i("onChargeResult()=" + isChargeSuccess);
        stopMonitor();
        showToast(mContext.getString(R.string.charge_result, isChargeSuccess));
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
