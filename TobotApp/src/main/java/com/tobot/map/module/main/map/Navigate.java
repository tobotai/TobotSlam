package com.tobot.map.module.main.map;

import android.content.Context;

import com.slamtec.slamware.robot.Location;
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
    private float x, y, yaw;

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
    }

    @Override
    public void onNavigateError() {
        Logger.i(BaseConstant.TAG, "onNavigateError()");
        stopMonitor();
        showToast(mContext.getString(R.string.navigate_error));
    }

    @Override
    public void onNavigateResult(boolean isNavigateSuccess) {
        Logger.i(BaseConstant.TAG, "onNavigationResult() isNavigateSuccess=" + isNavigateSuccess);
        stopMonitor();
        showToast(mContext.getString(R.string.navigate_result, isNavigateSuccess));
        if (mActivity != null) {
            mActivity.handleMoveResult(isNavigateSuccess);
        }
    }

    @Override
    public void onObstacleTrigger() {
        SlamManager.getInstance().cancelAction();
    }

    @Override
    public void onObstacleDisappear() {
        moveTo(x, y, yaw);
    }

    public void moveTo(LocationBean bean) {
        if (bean != null) {
            moveTo(bean.getX(), bean.getY(), bean.getYaw());
        }
    }

    public void moveTo(float x, float y, float yaw) {
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        startMonitor();
        Location location = new Location(x, y, 0);
        long tryTime = 0;
        if (MoveData.getInstance().getObstacleMode() == MoveData.MEET_OBSTACLE_AVOID) {
            tryTime = DataHelper.getInstance().getTryTimeMillis(mContext);
        }
        SlamManager.getInstance().moveTo(location, MoveData.getInstance().getMoveOption(), yaw, tryTime, this);
    }

    public void stop() {
        stopMonitor();
    }
}
