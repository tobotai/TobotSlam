package com.tobot.map.module.main;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.robot.Pose;
import com.tobot.slam.view.MapView;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2020/3/14
 */
public class MainHandle extends Handler {
    public static final int MSG_GET_ROBOT_POSE = 1;
    public static final int MSG_GET_STATUS = 2;
    public static final int MSG_SHOW_TOAST = 3;
    public static final int MSG_MAP_IS_UPDATE = 4;
    public static final int MSG_RELOCATION = 5;
    public static final int MSG_CLEAN_MAP = 6;
    public static final int MSG_SAVE_MAP = 7;
    public static final int MSG_GET_RSSI = 8;
    private MainActivity mMainActivity;
    private MapView mMapView;

    MainHandle(WeakReference<MainActivity> activityWeakReference, WeakReference<MapView> mapViewWeakReference) {
        super();
        mMainActivity = activityWeakReference.get();
        mMapView = mapViewWeakReference.get();
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (mMainActivity == null || mMapView == null) {
            return;
        }

        switch (msg.what) {
            case MSG_GET_ROBOT_POSE:
                mMainActivity.updatePoseShow((Pose) msg.obj);
                break;
            case MSG_GET_STATUS:
                Object[] objects = (Object[]) msg.obj;
                mMainActivity.updateStatus((Integer) objects[0], (Boolean) objects[1], (Integer) objects[2], (ActionStatus) objects[3]);
                break;
            case MSG_SHOW_TOAST:
                mMainActivity.showToast((String) msg.obj);
                break;
            case MSG_MAP_IS_UPDATE:
                mMainActivity.setMapUpdateStatus((Boolean) msg.obj);
                break;
            case MSG_RELOCATION:
                mMainActivity.relocationResult((Boolean) msg.obj);
                break;
            case MSG_CLEAN_MAP:
                mMainActivity.cleanMapResult((Boolean) msg.obj);
                break;
            case MSG_SAVE_MAP:
                mMainActivity.saveMapResult((Boolean) msg.obj);
                break;
            case MSG_GET_RSSI:
                mMainActivity.setRssi((Integer) msg.obj);
                break;
            default:
                break;
        }
    }
}
