package com.tobot.map.module.main;

import android.content.Context;
import android.os.Handler;

import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.slamtec.slamware.robot.Pose;
import com.tobot.map.util.NetworkUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.view.MapView;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
class MapHelper {
    private Context mContext;
    private Thread mapUpdate;
    private Handler mMainHandler;
    private MapView mMapView;
    private boolean isFirstRefresh;
    private int mRefreshCount;
    private boolean isStart;

    MapHelper(WeakReference<Context> contextWeakReference, WeakReference<Handler> handlerWeakReference, WeakReference<MapView> mapViewWeakReference) {
        mContext = contextWeakReference.get();
        mMainHandler = handlerWeakReference.get();
        mMapView = mapViewWeakReference.get();
        isFirstRefresh = true;
        startUpdateMap();
    }

    void destroy() {
        stopUpdateMap();
        cancelAction();
    }

    void cancelAction() {
        ThreadPoolManager.getInstance().execute(new CancelRunnable());
    }

    void updateMap() {
        isFirstRefresh = true;
        mRefreshCount = 0;
    }

    void startUpdateMap() {
        isStart = true;
        if (mapUpdate == null) {
            mapUpdate = new Thread(updateMapRunnable);
            mapUpdate.start();
        }
    }

    void stopUpdateMap() {
        isFirstRefresh = false;
        isStart = false;
        if (mapUpdate != null) {
            mapUpdate.interrupt();
            mapUpdate = null;
        }
        if (mMapView != null) {
            mMapView.setMapUpdate(false);
        }
    }

    private Runnable updateMapRunnable = new Runnable() {
        int cnt;

        @Override
        public void run() {
            cnt = 0;
            mRefreshCount = 0;

            while (isStart) {
                if (mMainHandler == null || mMapView == null) {
                    return;
                }

                try {
                    if ((cnt % 10) == 0) {
                        // 更新机器人当前姿态
                        Pose pose = SlamManager.getInstance().getPose();
                        mMapView.setRobotPose(pose);
                        mMainHandler.obtainMessage(MainHandle.MSG_GET_ROBOT_POSE, pose).sendToTarget();
                        // 更新机器人扫描的区域
                        mMapView.setLaserScan(SlamManager.getInstance().getLaserScan());
                        // 获取机器健康信息
                        mMapView.setHealth(SlamManager.getInstance().getRobotHealthInfo(), pose);
                        // 获取传感器信息
                        mMapView.setSensors(SlamManager.getInstance().getSensors(), SlamManager.getInstance().getSensorValues(), pose);
                    }

                    if ((cnt % 20) == 0) {
                        // 获取地图
                        if (isFirstRefresh || SlamManager.getInstance().isMapUpdate()) {
                            mRefreshCount++;
                            if (mRefreshCount > 3) {
                                mRefreshCount = 0;
                                isFirstRefresh = false;
                            }
                            // 更新地图
                            mMapView.setMap(SlamManager.getInstance().getMap());
                        } else {
                            mMapView.setMapUpdate(false);
                        }
                        // 获取虚拟墙
                        mMapView.setLines(ArtifactUsage.ArtifactUsageVirutalWall, SlamManager.getInstance().getLines(ArtifactUsage.ArtifactUsageVirutalWall));
                        // 获取轨道
                        mMapView.setLines(ArtifactUsage.ArtifactUsageVirtualTrack, SlamManager.getInstance().getLines(ArtifactUsage.ArtifactUsageVirtualTrack));
                        // 获取运动状态
                        mMapView.setRemainingMilestones(SlamManager.getInstance().getRemainingMilestones());
                        mMapView.setRemainingPath(SlamManager.getInstance().getRemainingPath());
                        // 获取机器状态
                        int battery = SlamManager.getInstance().getBatteryPercentage();
                        boolean isCharge = SlamManager.getInstance().isBatteryCharging();
                        int locationQuality = SlamManager.getInstance().getLocalizationQuality();
                        // 获取机器人信息
                        ActionStatus actionStatus = SlamManager.getInstance().getRemainingActionStatus();
                        mMainHandler.obtainMessage(MainHandle.MSG_GET_STATUS, new Object[]{battery, isCharge, locationQuality, actionStatus}).sendToTarget();
                    }

                    if ((cnt % 30) == 0) {
                        // 获取充电桩位置
                        mMapView.setHomePose(SlamManager.getInstance().getHomePose());
                    }

                    if (cnt % 60 == 0) {
                        // 显示网络信号
                        mMainHandler.obtainMessage(MainHandle.MSG_GET_RSSI, NetworkUtils.getRssi(mContext)).sendToTarget();
                    }

                    Thread.sleep(33);
                    cnt++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static class CancelRunnable implements Runnable {

        @Override
        public void run() {
            SlamManager.getInstance().cancelAction();
        }
    }
}
