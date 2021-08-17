package com.tobot.map.module.main;

import android.content.Context;

import com.tobot.map.R;
import com.tobot.map.module.common.MoveData;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnObstacleListener;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2020/5/1
 */
public abstract class AbstractPathMonitor implements OnObstacleListener {
    protected Context mContext;
    protected MainActivity mActivity;

    public AbstractPathMonitor(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
        mContext = contextWeakReference.get();
        mActivity = activityWeakReference.get();
    }

    protected void startMonitor() {
        if (MoveData.getInstance().getObstacleMode() == MoveData.MEET_OBSTACLE_SUSPEND) {
            SlamManager.getInstance().startMonitorObstacle(this);
        }
    }

    protected void stopMonitor() {
        SlamManager.getInstance().stopMonitorObstacle();
    }

    protected void showToast(String tips) {
        if (mActivity != null) {
            mActivity.showToast(tips);
        }
    }

    protected void showRelocateTips() {
        if (mActivity != null) {
            mActivity.showRelocateTips();
        }
    }

    protected void handleRelocateResult(boolean isRelocateSuccess) {
        if (mActivity != null) {
            mActivity.handleRelocateResult(isRelocateSuccess);
        }
    }

    protected void handleMoveFail(boolean isJudgeReason) {
        if (isJudgeReason && isSystemStop()) {
            return;
        }

        if (mActivity != null) {
            mActivity.handleMoveFail();
        }
    }

    protected boolean isSystemStop() {
        if (SlamManager.getInstance().isSystemEmergencyStop()) {
            showToast(mContext.getString(R.string.emergency_stop_tips));
            return true;
        }

        if (SlamManager.getInstance().isSystemBrakeStop()) {
            showToast(mContext.getString(R.string.break_stop_tips));
            return true;
        }

        if (SlamManager.getInstance().isDirectCharge()) {
            showToast(mContext.getString(R.string.direct_charge_to_navigate_tips));
            return true;
        }

        return false;
    }
}
