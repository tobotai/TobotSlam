package com.tobot.map.module.main.edit;

import com.slamtec.slamware.robot.ArtifactUsage;
import com.slamtec.slamware.robot.Map;
import com.tobot.slam.SlamManager;
import com.tobot.slam.view.MapView;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
class EditMapHelper {
    private final MapView mMapView;
    private MapThread mMapThread;
    private boolean isStart;

    EditMapHelper(WeakReference<MapView> reference) {
        mMapView = reference.get();
    }

    void startUpdateMap() {
        isStart = true;
        if (mMapThread == null) {
            mMapThread = new MapThread();
            mMapThread.start();
        }
    }

    void stopUpdateMap() {
        isStart = false;
        if (mMapThread != null) {
            mMapThread.interrupt();
            mMapThread = null;
        }
    }

    private class MapThread extends Thread {

        @Override
        public void run() {
            int count = 0;
            Map map = null;
            while (isStart) {
                if (mMapView == null) {
                    return;
                }

                try {
                    if ((count % 10) == 0) {
                        if (map == null) {
                            map = SlamManager.getInstance().getMap();
                            mMapView.setMap(map);
                        }
                        // 更新机器人当前姿态
                        mMapView.setRobotPose(SlamManager.getInstance().getPose());
                        // 更新机器人扫描的区域
                        mMapView.setLaserScan(SlamManager.getInstance().getLaserScan());
                    }

                    if (!isStart) {
                        return;
                    }

                    if ((count % 20) == 0) {
                        // 获取虚拟墙
                        mMapView.setLines(ArtifactUsage.ArtifactUsageVirutalWall, SlamManager.getInstance().getLines(ArtifactUsage.ArtifactUsageVirutalWall));
                        // 获取轨道
                        mMapView.setLines(ArtifactUsage.ArtifactUsageVirtualTrack, SlamManager.getInstance().getLines(ArtifactUsage.ArtifactUsageVirtualTrack));
                    }

                    if (!isStart) {
                        return;
                    }

                    if (count % 50 == 0) {
                        // 获取充电桩位置
                        mMapView.setHomePose(SlamManager.getInstance().getHomePose());
                    }

                    if (isStart) {
                        count++;
                        if (count > 60000) {
                            count = 0;
                        }
                        sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
