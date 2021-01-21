package com.tobot.map.module.main;

import android.content.Context;

import com.tobot.map.module.common.MoveData;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnObstacleListener;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2020/5/1
 */
public abstract class AbsPathMonitor implements OnObstacleListener {
    protected Context mContext;
    private MainActivity mActivity;

    public AbsPathMonitor(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
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
}
