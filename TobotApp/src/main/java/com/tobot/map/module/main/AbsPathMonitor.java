package com.tobot.map.module.main;

import android.content.Context;
import android.os.Handler;

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
    private Handler mHandler;

    public AbsPathMonitor(WeakReference<Context> contextWeakReference, WeakReference<Handler> handlerWeakReference) {
        mContext = contextWeakReference.get();
        mHandler = handlerWeakReference.get();
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
        if (mHandler != null) {
            mHandler.obtainMessage(MainHandle.MSG_SHOW_TOAST, tips).sendToTarget();
        }
    }
}
