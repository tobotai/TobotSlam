package com.tobot.map.module.main;

import android.content.Context;
import android.text.TextUtils;

import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.slamtec.slamware.robot.Pose;
import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
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
    private MapThread mMapThread;
    private MapDataThread mMapDataThread;
    private MainActivity mActivity;
    private MapView mMapView;
    private boolean isStart, isFirstRefresh, isLastCharge, isInit;
    private int mRefreshCount, mQuality;
    /**
     * 避免开机电量为0不显示的问题
     */
    private int mBattery = -100;
    private ActionStatus mActionStatus;
    private float mX, mY, mYaw;

    MapHelper(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference, WeakReference<MapView> mapViewWeakReference) {
        mContext = contextWeakReference.get();
        mActivity = activityWeakReference.get();
        mMapView = mapViewWeakReference.get();
        isInit = true;
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
        if (mMapDataThread != null) {
            mMapDataThread.interrupt();
        }
    }

    void startUpdateMap() {
        isFirstRefresh = true;
        mRefreshCount = 0;
        isStart = true;
        if (mMapThread == null) {
            mMapThread = new MapThread();
            mMapThread.start();
        }

        if (mMapDataThread == null) {
            mMapDataThread = new MapDataThread();
            mMapDataThread.start();
        }
    }

    void stopUpdateMap() {
        isFirstRefresh = false;
        isStart = false;
        if (mMapThread != null) {
            mMapThread.interrupt();
            mMapThread = null;
        }

        if (mMapDataThread != null) {
            mMapDataThread.interrupt();
            mMapDataThread = null;
        }

        if (mMapView != null) {
            mMapView.setMapUpdate(false, true);
        }
    }

    private class MapThread extends Thread {

        @Override
        public void run() {
            handleControlPanelSoftwareVersion();
            int count = 0;
            int mRssid = 1;

            while (isStart) {
                if (mActivity == null || mMapView == null) {
                    return;
                }

                try {
                    if ((count % 10) == 0) {
                        // 更新机器人当前姿态
                        Pose pose = SlamManager.getInstance().getPose();
                        mMapView.setRobotPose(pose);
                        updatePoseShow(pose);
                        // 更新机器人扫描的区域
                        mMapView.setLaserScan(SlamManager.getInstance().getLaserScan());
                        // 获取机器健康信息
                        mMapView.setHealth(SlamManager.getInstance().getRobotHealthInfo(), pose);
                        // 获取传感器信息
                        mMapView.setSensors(SlamManager.getInstance().getSensors(), SlamManager.getInstance().getSensorValues(), pose);
                    }

                    if (!isStart) {
                        return;
                    }

                    if ((count % 20) == 0) {
                        // 获取虚拟墙
                        mMapView.setLines(ArtifactUsage.ArtifactUsageVirutalWall, SlamManager.getInstance().getLines(ArtifactUsage.ArtifactUsageVirutalWall));
                        // 获取轨道
                        mMapView.setLines(ArtifactUsage.ArtifactUsageVirtualTrack, SlamManager.getInstance().getLines(ArtifactUsage.ArtifactUsageVirtualTrack));
                        // 获取剩余路径
                        mMapView.setRemaining(SlamManager.getInstance().getRemainingMilestones(), SlamManager.getInstance().getRemainingPath());
                        // 获取机器状态
                        updateStatus();
                    }

                    if (!isStart) {
                        return;
                    }

                    if (count % 50 == 0) {
                        // 获取充电桩位置
                        mMapView.setHomePose(SlamManager.getInstance().getHomePose());
                    }

                    if (count % 60 == 0) {
                        // 显示网络信号
                        int rssiId = NetworkUtils.getRssi(mContext);
                        if (rssiId != mRssid) {
                            mRssid = rssiId;
                            mActivity.setRssi(rssiId, getIdTips(rssiId));
                        }
                    }

                    if (isStart) {
                        Thread.sleep(33);
                        count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        private void updatePoseShow(Pose pose) {
            if (pose != null) {
                float x = pose.getX();
                float y = pose.getY();
                float yaw = pose.getYaw();
                if (mX - x != 0 || mY - y != 0 || mYaw - yaw != 0) {
                    mX = x;
                    mY = y;
                    mYaw = yaw;
                    mActivity.updatePoseShow(pose);
                }
            }
        }

        private void updateStatus() {
            int battery = SlamManager.getInstance().getBatteryPercentage();
            boolean isCharge = SlamManager.getInstance().isBatteryCharging();
            int locationQuality = SlamManager.getInstance().getLocalizationQuality();
            // 获取机器人信息
            ActionStatus actionStatus = SlamManager.getInstance().getRemainingActionStatus();
            if (mBattery != battery || isLastCharge != isCharge || mQuality != locationQuality || mActionStatus != actionStatus) {
                mBattery = battery;
                isLastCharge = isCharge;
                mQuality = locationQuality;
                mActionStatus = actionStatus;
                mActivity.updateStatus(battery, isCharge, locationQuality, actionStatus);
            }
        }

        private String getIdTips(int id) {
            int excellent = -60;
            if (id >= excellent) {
                return mContext.getString(R.string.signal_1);
            }

            int good = -80;
            if (id >= good) {
                return mContext.getString(R.string.signal_2);
            }

            return mContext.getString(R.string.signal_3);
        }

        private void handleControlPanelSoftwareVersion() {
            // 只发送一遍
            if (isInit) {
                isInit = false;
                String version = SlamManager.getInstance().getControlPanelSoftwareVersion();
                Logger.i(BaseConstant.TAG, "version=" + version);
                String errorVersion = "1.0";
                if (TextUtils.equals(errorVersion, version) && mActivity != null) {
                    mActivity.showTipsDialog(mContext.getString(R.string.control_software_version_not_match));
                }
            }
        }
    }

    private class MapDataThread extends Thread {
        @Override
        public void run() {
            super.run();
            int refreshCount = 3;
            boolean isSetMap = false;
            long delayTime;

            while (isStart) {
                if (mMapView == null) {
                    return;
                }

                try {
                    // 更新地图
                    boolean isMapUpdate = SlamManager.getInstance().isMapUpdate();
                    if (isFirstRefresh || isMapUpdate) {
                        if (isMapUpdate) {
                            delayTime = 500;
                        } else {
                            delayTime = 1000;
                            mRefreshCount++;
                            if (mRefreshCount > refreshCount) {
                                mRefreshCount = 0;
                                isFirstRefresh = false;
                            }
                        }

                        isSetMap = false;
                        mMapView.setMap(SlamManager.getInstance().getMap());
                    } else {
                        delayTime = 6000;
                        if (!isSetMap) {
                            isSetMap = true;
                            mMapView.setMapUpdate(false, false);
                        }
                    }

                    if (isStart) {
                        Thread.sleep(delayTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!isStart) {
                        return;
                    }
                }
            }
        }
    }

    private static class CancelRunnable implements Runnable {

        @Override
        public void run() {
            SlamManager.getInstance().cancelAction();
        }
    }
}
