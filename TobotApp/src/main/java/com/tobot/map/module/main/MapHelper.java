package com.tobot.map.module.main;

import android.content.Context;

import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.slamtec.slamware.robot.Pose;
import com.tobot.map.R;
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
    private MapRunnable mMapRunnable;
    private MainActivity mActivity;
    private MapView mMapView;
    private boolean isFirstRefresh, isStart, isLastCharge;
    private int mRefreshCount, mBattery, mQuality;
    private ActionStatus mActionStatus;
    private float mX, mY, mYaw;
    private int mRssid = 1;

    MapHelper(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference, WeakReference<MapView> mapViewWeakReference) {
        mContext = contextWeakReference.get();
        mActivity = activityWeakReference.get();
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
        if (mMapRunnable == null) {
            mRssid = 1;
            isStart = true;
            mMapRunnable = new MapRunnable();
            ThreadPoolManager.getInstance().execute(mMapRunnable);
        }
    }

    void stopUpdateMap() {
        isFirstRefresh = false;
        isStart = false;
        if (mMapRunnable != null) {
            ThreadPoolManager.getInstance().cancel(mMapRunnable);
            mMapRunnable = null;
        }
        if (mMapView != null) {
            mMapView.setMapUpdate(false);
        }
    }

    void setRssi(int rssiId) {
        mRssid = rssiId;
    }

    private class MapRunnable implements Runnable {

        @Override
        public void run() {
            int cnt = 0;
            mRefreshCount = 0;

            while (isStart) {
                if (mActivity == null || mMapView == null) {
                    return;
                }

                try {
                    if ((cnt % 10) == 0) {
                        // 更新机器人当前姿态
                        Pose pose = SlamManager.getInstance().getPose();
                        mMapView.setRobotPose(pose);
                        if (pose != null) {
                            float x = pose.getX();
                            float y = pose.getY();
                            float yaw = pose.getYaw();
                            if (mX != x || mY != y || mYaw != yaw) {
                                mX = x;
                                mY = y;
                                mYaw = yaw;
                                mActivity.updatePoseShow(pose);
                            }
                        }
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
                        if (mBattery != battery || isLastCharge != isCharge || mQuality != locationQuality || mActionStatus != actionStatus) {
                            mBattery = battery;
                            isLastCharge = isCharge;
                            mQuality = locationQuality;
                            mActionStatus = actionStatus;
                            mActivity.updateStatus(battery, isCharge, locationQuality, actionStatus);
                        }
                    }

                    if ((cnt % 30) == 0) {
                        // 获取充电桩位置
                        mMapView.setHomePose(SlamManager.getInstance().getHomePose());
                    }

                    if (cnt % 60 == 0) {
                        // 显示网络信号
                        int rssiId = NetworkUtils.getRssi(mContext);
                        if (rssiId != mRssid) {
                            mRssid = rssiId;
                            mActivity.setRssi(rssiId, getRssiTips(rssiId));
                        }
                    }

                    Thread.sleep(33);
                    cnt++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private String getRssiTips(int rssiId) {
            String tips;
            if (rssiId >= -60) {
                tips = mContext.getString(R.string.signal_1);
            } else if (rssiId >= -80) {
                tips = mContext.getString(R.string.signal_2);
            } else {
                tips = mContext.getString(R.string.signal_3);
            }
            return tips;
        }
    }

    private static class CancelRunnable implements Runnable {

        @Override
        public void run() {
            SlamManager.getInstance().cancelAction();
        }
    }
}
