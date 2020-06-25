package com.tobot.map.module.main;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.slamtec.slamware.log.LogData;
import com.slamtec.slamware.log.customer.ICustomerLogReceiver;
import com.slamtec.slamware.log.customer.ReadResult;
import com.slamtec.slamware.log.customer.ResultCode;
import com.slamtec.slamware.robot.ImpactSensorInfo;
import com.slamtec.slamware.robot.ImpactSensorValue;
import com.slamtec.slamware.robot.SensorType;
import com.slamtec.slamware.utils.StdPair;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.util.LogUtils;
import com.tobot.slam.SlamManager;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author houdeming
 * @date 2020/5/1
 */
public abstract class AbsPathMonitor {
    protected Context mContext;
    private Handler mHandler;
    private LogThread mLogThread;
    private String mLastPathFind;
    private boolean isStart, isMonitorPath, isSensorsTrigger;
    private int mSensorsDisappearCount;

    public AbsPathMonitor(WeakReference<Context> contextWeakReference, WeakReference<Handler> handlerWeakReference) {
        mContext = contextWeakReference.get();
        mHandler = handlerWeakReference.get();
    }

    protected void startMonitor() {
        if (MoveData.getInstance().getObstacleMode() == MoveData.MEET_OBSTACLE_SUSPEND) {
            if (mLogThread == null) {
                mLastPathFind = "";
                isStart = true;
                isMonitorPath = true;
                isSensorsTrigger = false;
                mSensorsDisappearCount = 0;
                // 监听导航的log，只有导航的时候会有log
                mLogThread = new LogThread();
                mLogThread.start();
            }
        }
    }

    protected void stopMonitor() {
        isStart = false;
        if (mLogThread != null) {
            mLogThread.interrupt();
            mLogThread = null;
        }
        isMonitorPath = false;
    }

    protected void showToast(String tips) {
        if (mHandler != null) {
            mHandler.obtainMessage(MainHandle.MSG_SHOW_TOAST, tips).sendToTarget();
        }
    }

    private class LogThread extends Thread {
        @Override
        public void run() {
            super.run();
            ICustomerLogReceiver customerLogReceiver = SlamManager.getInstance().getCustomerLogReceiver();
            if (customerLogReceiver == null) {
                return;
            }

            while (isStart) {
                if (isMonitorPath) {
                    try {
                        StdPair<ResultCode, ReadResult> resultCodeReadResultStdPair = customerLogReceiver.recvLogs(1);
                        if (resultCodeReadResultStdPair != null) {
                            ArrayList<LogData> logs = resultCodeReadResultStdPair.getSecond().getLogs();
                            // 如果没有log的话，大小为0
                            if (logs != null && !logs.isEmpty()) {
                                for (LogData data : logs) {
                                    if (isPathFindSwitch(data.getStringOfLog())) {
                                        sendPathFindSwitch();
                                        // 路径变化后，去检测传感器
                                        isMonitorPath = false;
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    continue;
                }

                // 当路径切换后，检测传感器，没有检测器触发的话再继续行走
                if (isSensorsTrigger()) {
                    isSensorsTrigger = true;
                    mSensorsDisappearCount = 0;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    continue;
                }

                // 没有超声波触发的情况，暂停一会再去检测
                if (!isSensorsTrigger) {
                    isSensorsTrigger = true;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }

                // 连续3次没有触发的话再行走，避免误触发
                mSensorsDisappearCount++;
                if (mSensorsDisappearCount > 2) {
                    mSensorsDisappearCount = 0;
                    // 继续检测路径变化
                    isMonitorPath = true;
                    isSensorsTrigger = false;
                    sendKeepMove();
                    continue;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        private boolean isPathFindSwitch(String log) {
            if (!TextUtils.isEmpty(log)) {
                try {
                    JSONObject object = new JSONObject(log);
                    String keyword = object.optString("keyword");
                    LogUtils.i("path keyword=" + keyword);
                    if (!TextUtils.isEmpty(keyword)) {
//                        if (mHandler != null) {
//                            mHandler.obtainMessage(MainHandle.MSG_PATH_FIND, keyword).sendToTarget();
//                        }

                        // requestLocalPathFind：局部的，requestGlobalPathFind：全局的，当重新规划路径后会有这个log，会连续返回
                        if (TextUtils.equals("requestGlobalPathFind", keyword) || TextUtils.equals("requestLocalPathFind", keyword)) {
                            if (TextUtils.isEmpty(mLastPathFind)) {
                                mLastPathFind = keyword;
                                return false;
                            }

                            // 如果当前跟上一次的不一样，则代表路径重新规划了
                            if (!TextUtils.equals(keyword, mLastPathFind)) {
                                mLastPathFind = "";
                                return true;
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        private boolean isSensorsTrigger() {
            List<ImpactSensorInfo> sensorInfoList = SlamManager.getInstance().getSensors();
            HashMap<Integer, ImpactSensorValue> sensorValueHashMap = SlamManager.getInstance().getSensorValues();

            if ((sensorInfoList == null || sensorInfoList.isEmpty()) || (sensorValueHashMap == null || sensorValueHashMap.isEmpty())) {
                return false;
            }

            for (ImpactSensorInfo info : sensorInfoList) {
                if (info == null) {
                    continue;
                }

                float sensorValue = -1f;
                int id = info.getSensorId();
                for (Map.Entry<Integer, ImpactSensorValue> entry : sensorValueHashMap.entrySet()) {
                    if (id == entry.getKey()) {
                        ImpactSensorValue impactSensorValue = entry.getValue();
                        if (impactSensorValue != null) {
                            sensorValue = impactSensorValue.getValue();
                        }
                        break;
                    }
                }

                if (sensorValue < 0) {
                    continue;
                }

                // 超声波传感器，触发的话不为0，没触发的话为0
                if (info.getKind() == SensorType.Sonar && sensorValue != 0) {
                    return true;
                }
            }

            return false;
        }
    }

    private void sendPathFindSwitch() {
        if (isStart) {
            LogUtils.i("pathFindSwitch()");
            pathFindSwitch();
        }
    }

    private void sendKeepMove() {
        if (isStart) {
            LogUtils.i("keepMove()");
            keepMove();
        }
    }

    /**
     * 路径改变
     */
    public abstract void pathFindSwitch();

    /**
     * 继续运动
     */
    public abstract void keepMove();
}
