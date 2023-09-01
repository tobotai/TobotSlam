package com.tobot.map.module.main;

import android.content.Context;
import android.text.TextUtils;

import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.slamtec.slamware.robot.Map;
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
    private final Context mContext;
    private final MainActivity mActivity;
    private final MapView mMapView;
    private boolean isInit, isStart, isLastCharge, isLastLowBattery, isLastMapUpdate, isSetMapCenter;
    private MapThread mMapThread;
    /**
     * 避免开机电量为0不显示的问题
     */
    private int mLastBattery = -1;
    private int mLastQuality;
    private ActionStatus mLastActionStatus;
    private float mLastX, mLastY;
    /**
     * 360度的弧度是6.28，只要比360度大就行
     */
    private float mLastYaw = 10f;
    private Map mMap;

    MapHelper(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference, WeakReference<MapView> mapViewWeakReference) {
        mContext = contextWeakReference.get();
        mActivity = activityWeakReference.get();
        mMapView = mapViewWeakReference.get();
        isInit = true;
    }

    void destroy() {
        stopUpdateMap();
        cancelAction();
    }

    void cancelAction() {
        ThreadPoolManager.getInstance().execute(new CancelRunnable());
    }

    void startUpdateMap() {
        mLastBattery = -1;
        isStart = true;
        if (mMapThread == null) {
            mMapThread = new MapThread();
            mMapThread.start();
        }
    }

    void stopUpdateMap() {
        mMap = null;
        isStart = false;
        if (mMapThread != null) {
            mMapThread.interrupt();
            mMapThread = null;
        }
    }

    void chargeResult(boolean isSuccess) {
        // 避免充电不成功的情况
        if (!isSuccess) {
            isLastLowBattery = false;
        }
    }

    void editMap() {
        // 重新更新刷新地图
        mMap = null;
        isSetMapCenter = true;
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
                        if (mMap == null || SlamManager.getInstance().isMapUpdate()) {
                            mMap = SlamManager.getInstance().getMap();
                            mMapView.setMap(mMap);
                            if (isSetMapCenter) {
                                isSetMapCenter = false;
                                mMapView.setCentred();
                            }
                        }
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
                        // 获取重定位的区域
                        mMapView.setArea(SlamManager.getInstance().getRelocationArea());
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
                        count++;
                        if (count > 60000) {
                            count = 0;
                        }
                        sleep(33);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        private void updatePoseShow(Pose pose) {
            if (pose != null) {
                float accuracy = 100.0f;
                float x = Math.round(pose.getX() * accuracy) / accuracy;
                float y = Math.round(pose.getY() * accuracy) / accuracy;
                float yaw = Math.round(pose.getYaw() * accuracy) / accuracy;
                if (x - mLastX != 0 || y - mLastY != 0 || yaw - mLastYaw != 0) {
                    mLastX = x;
                    mLastY = y;
                    mLastYaw = yaw;
                    mActivity.updatePoseShow(x, y, yaw);
                }
            }
        }

        private void updateStatus() {
            int battery = SlamManager.getInstance().getBatteryPercentage();
            boolean isCharge = SlamManager.getInstance().isBatteryCharging();
            int locationQuality = SlamManager.getInstance().getLocalizationQuality();
            // 获取机器人信息
            ActionStatus actionStatus = SlamManager.getInstance().getRemainingActionStatus();
            boolean isMapUpdate = SlamManager.getInstance().isMapUpdate();
            if (mLastBattery != battery || isLastCharge != isCharge || mLastQuality != locationQuality || mLastActionStatus != actionStatus || isLastMapUpdate != isMapUpdate) {
                mLastBattery = battery;
                isLastCharge = isCharge;
                mLastQuality = locationQuality;
                mLastActionStatus = actionStatus;
                isLastMapUpdate = isMapUpdate;
                boolean isDockingStatus = SlamManager.getInstance().isDockingStatus();
                boolean isDirectCharge = SlamManager.getInstance().isDirectCharge();
                String chargeMode = getChargeMode(isDockingStatus, isDirectCharge);
                String status = actionStatus != null ? actionStatus.toString() : mContext.getString(R.string.unknown);
                String mappingStatus = mContext.getString(isMapUpdate ? R.string.mapping_true : R.string.mapping_false);
                mActivity.updateStatus(mappingStatus, battery, isCharge, chargeMode, locationQuality, status);
            }

            handleLowBattery(battery, isCharge);
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

        private String getChargeMode(boolean isDockingStatus, boolean isDirectCharge) {
            if (isDockingStatus) {
                return mContext.getString(R.string.charging_docking);
            }

            if (isDirectCharge) {
                return mContext.getString(R.string.charging_direct);
            }

            return "";
        }

        private void handleLowBattery(int battery, boolean isCharge) {
            if (battery <= 0) {
                return;
            }

            boolean isLowBattery = battery <= DataHelper.getInstance().getLowBattery(mContext);
            DataHelper.getInstance().setLowBattery(isLowBattery);
            if (isLowBattery) {
                if (!isCharge && !isLastLowBattery) {
                    // 避免急停、刹车按下的情况
                    if (SlamManager.getInstance().isSystemStop()) {
                        return;
                    }

                    isLastLowBattery = true;
                    DataHelper.getInstance().recordImportantInfo(mContext, "low battery=" + battery);
                    if (mActivity != null) {
                        mActivity.handleLowBattery(true);
                    }
                }
                return;
            }

            if (isLastLowBattery) {
                isLastLowBattery = false;
                if (mActivity != null) {
                    mActivity.handleLowBattery(false);
                }
            }
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

    private static class CancelRunnable implements Runnable {

        @Override
        public void run() {
            SlamManager.getInstance().cancelAction();
        }
    }
}
