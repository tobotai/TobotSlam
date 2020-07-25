package com.tobot.map.module.main.map;

import android.content.Context;
import android.os.Handler;

import com.slamtec.slamware.robot.Location;
import com.tobot.map.R;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.module.main.AbsPathMonitor;
import com.tobot.map.util.LogUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnNavigateListener;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
public class Navigate extends AbsPathMonitor implements OnNavigateListener {
    private float x, y, yaw;

    public Navigate(WeakReference<Context> contextWeakReference, WeakReference<Handler> handlerWeakReference) {
        super(contextWeakReference, handlerWeakReference);
    }

    @Override
    public void onNavigateStartTry() {
        LogUtils.i("onNavigateStartTry()");
    }

    @Override
    public void onNavigateRemind() {
        LogUtils.i("onNavigateRemind()");
        showToast(mContext.getString(R.string.navigate_remind));
    }

    @Override
    public void onNavigateSensorTrigger(boolean isEnabled) {
        LogUtils.i("onNavigateSensorTrigger() isEnabled=" + isEnabled);
    }

    @Override
    public void onNavigateError() {
        LogUtils.i("onNavigateError()");
        stopMonitor();
        showToast(mContext.getString(R.string.navigate_error));
    }

    @Override
    public void onNavigateResult(boolean isNavigateSuccess) {
        LogUtils.i("onNavigationResult() isNavigateSuccess=" + isNavigateSuccess);
        stopMonitor();
        showToast(mContext.getString(R.string.navigate_result, isNavigateSuccess));
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
        SlamManager.getInstance().moveTo(location, MoveData.getInstance().getMoveOption(), yaw, 0, this);
    }

    public void stop() {
        stopMonitor();
    }
}
