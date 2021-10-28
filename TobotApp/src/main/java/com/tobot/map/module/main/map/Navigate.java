package com.tobot.map.module.main.map;

import android.content.Context;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.AbstractPathMonitor;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.module.main.MainActivity;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnNavigateListener;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
public class Navigate extends AbstractPathMonitor implements OnNavigateListener {
    private LocationBean mLocationBean;

    public Navigate(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
        super(contextWeakReference, activityWeakReference);
    }

    @Override
    public void onNavigateStartTry() {
        Logger.i(BaseConstant.TAG, "onNavigateStartTry()");
    }

    @Override
    public void onNavigateRemind() {
        Logger.i(BaseConstant.TAG, "onNavigateRemind()");
        showToast(mContext.getString(R.string.navigate_remind));
    }

    @Override
    public void onNavigateSensorTrigger(boolean isEnabled) {
        Logger.i(BaseConstant.TAG, "onNavigateSensorTrigger() isEnabled=" + isEnabled);
        showToast("sensor isEnabled=" + isEnabled);
    }

    @Override
    public void onNavigateRelocateBegin() {
        Logger.i(BaseConstant.TAG, "onNavigateRelocateBegin()");
        showRelocateTips();
    }

    @Override
    public void onNavigateRelocateEnd(boolean isRelocateSuccess) {
        Logger.i(BaseConstant.TAG, "onNavigateRelocateEnd() isRelocateSuccess=" + isRelocateSuccess);
        handleRelocateResult(isRelocateSuccess);
    }

    @Override
    public void onNavigateSetPose(boolean isFinish) {
        Logger.i(BaseConstant.TAG, "onNavigateSetPose() isFinish=" + isFinish);
        handleNavigateSetPose(isFinish);
    }

    @Override
    public void onNavigateError() {
        Logger.i(BaseConstant.TAG, "onNavigateError()");
        stop();
        showToast(mContext.getString(R.string.navigate_error));
    }

    @Override
    public void onNavigateResult(boolean isNavigateSuccess) {
        Logger.i(BaseConstant.TAG, "onNavigationResult() isNavigateSuccess=" + isNavigateSuccess);
        stop();
        showToast(mContext.getString(R.string.navigate_result, isNavigateSuccess));
        if (!isNavigateSuccess) {
            handleMoveFail(true);
        }
    }

    @Override
    public void onObstacleTrigger() {
        Logger.i(BaseConstant.TAG, "onObstacleTrigger()");
        SlamManager.getInstance().cancelAction();
    }

    @Override
    public void onObstacleDisappear() {
        Logger.i(BaseConstant.TAG, "onObstacleDisappear()");
        moveTo(mLocationBean);
    }

    public void moveTo(float x, float y, float yaw) {
        LocationBean bean = new LocationBean();
        bean.setX(x);
        bean.setY(y);
        bean.setYaw(yaw);
        moveTo(bean);
    }

    public void moveTo(LocationBean bean) {
        mLocationBean = bean;
        if (bean != null) {
            startMonitor();
            long tryTime = 0;
            if (MoveData.getInstance().getObstacleMode() == MoveData.MEET_OBSTACLE_AVOID) {
                tryTime = DataHelper.getInstance().getTryTimeMillis(mContext);
            }
            SlamManager.getInstance().moveTo(bean, MoveData.getInstance().getMoveOption(), tryTime, this);
        }
    }

    public void stop() {
        stopMonitor();
    }
}
