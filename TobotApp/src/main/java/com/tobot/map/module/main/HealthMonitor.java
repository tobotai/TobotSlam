package com.tobot.map.module.main;

import android.content.Context;
import android.text.TextUtils;

import com.slamtec.slamware.robot.HealthInfo;
import com.slamtec.slamware.robot.ImpactSensorInfo;
import com.slamtec.slamware.robot.ImpactSensorValue;
import com.slamtec.slamware.robot.SensorType;
import com.tobot.map.R;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author houdeming
 * @date 2023/08/30
 */
public class HealthMonitor extends Thread {
    private static final long TIME_DELAY = 150;
    private final Context mContext;
    private final OnHealthListener mListener;
    private int mLastSize;
    private List<String> mLastList;
    private boolean isFatal, isStart;

    public HealthMonitor(Context context, OnHealthListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public void run() {
        super.run();
        List<String> list;
        while (isStart) {
            try {
                isFatal = false;
                list = new ArrayList<>();
                if (DataHelper.getInstance().isLowBattery()) {
                    list.add(mContext.getString(R.string.low_battery));
                }
                monitorSensor(list);
                monitorSystem(list);
                callbackHealth(list);
                sleep(TIME_DELAY);
            } catch (Exception e) {
                e.printStackTrace();
                if (!isStart) {
                    return;
                }
            }
        }
    }

    public void startMonitor() {
        mLastSize = 0;
        mLastList = null;
        isStart = true;
        start();
    }

    public void stopMonitor() {
        isStart = false;
        interrupt();
    }

    private void monitorSensor(List<String> list) {
        List<ImpactSensorInfo> infoList = SlamManager.getInstance().getSensors();
        if (infoList == null || infoList.isEmpty()) {
            return;
        }

        HashMap<Integer, ImpactSensorValue> hashMap = SlamManager.getInstance().getSensorValues();
        if (hashMap == null || hashMap.isEmpty()) {
            return;
        }

        for (ImpactSensorInfo info : infoList) {
            if (info == null) {
                continue;
            }

            int id = info.getSensorId();
            float sensorValue = getSensorValue(id, hashMap);
            if (sensorValue < 0) {
                continue;
            }

            SensorType sensorType = info.getKind();
            String str = getSensorStr(sensorType, sensorValue, id);
            if (!TextUtils.isEmpty(str)) {
                list.add(str);
            }
        }
    }

    private float getSensorValue(int id, HashMap<Integer, ImpactSensorValue> hashMap) {
        float sensorValue = -1f;
        for (Map.Entry<Integer, ImpactSensorValue> entry : hashMap.entrySet()) {
            if (id == entry.getKey()) {
                ImpactSensorValue impactSensorValue = entry.getValue();
                if (impactSensorValue != null) {
                    sensorValue = impactSensorValue.getValue();
                }
                break;
            }
        }

        return sensorValue;
    }

    private String getSensorStr(SensorType sensorType, float sensorValue, int id) {
        switch (sensorType) {
            case Bumper:
                // 碰撞传感器，触发的话为0，没触发的话不为0
                if (sensorValue == 0) {
                    return mContext.getString(R.string.sensor_bumper_trigger);
                }
                break;
            case Cliff:
                // 防跌落传感器，触发的话为0，没触发的话不为0
                if (sensorValue == 0) {
                    return mContext.getString(R.string.sensor_cliff_trigger, id);
                }
                break;
            default:
                break;
        }

        return "";
    }

    private void monitorSystem(List<String> list) {
        HealthInfo healthInfo = SlamManager.getInstance().getRobotHealthInfo();
        if (healthInfo != null) {
            List<HealthInfo.BaseError> errors = healthInfo.getErrors();
            if (errors != null && !errors.isEmpty()) {
                for (HealthInfo.BaseError baseError : errors) {
                    int errorType = baseError.getComponentErrorType();
                    int errorCode = baseError.getErrorCode();
                    String str = getError(errorType, errorCode);
                    if (!TextUtils.isEmpty(str)) {
                        list.add(str);
                    }
                }
            }
        }
    }

    private String getError(int errorType, int errorCode) {
        // 定义的字符串多个单词中间不能有空格，否则不显示
        switch (errorType) {
            case HealthInfo.BaseError.BaseComponentErrorTypeSystemBrakeReleased:
                // 刹车按钮
                return mContext.getString(R.string.break_stop_tips);
            case HealthInfo.BaseError.BaseComponentErrorTypeSystemEmergencyStop:
                // 急停按钮
                return mContext.getString(R.string.emergency_stop_tips);
            case HealthInfo.BaseError.BaseComponentErrorTypeSystemCtrlBusDisconnected:
                // 底盘内部系统连接断开
                return mContext.getString(R.string.control_bus_disconnect);
            default:
                break;
        }

        // 自定义透传
        switch (errorCode) {
            case SlamCode.CODE_MOTOR_ERROR:
                isFatal = true;
                // 电机异常
                return mContext.getString(R.string.motor_error);
            case SlamCode.CODE_BATTERY_COMMUNICATE_ERROR:
                // 电池通信异常
                return mContext.getString(R.string.battery_communicate_error);
            default:
                break;
        }

        return "";
    }

    private void callbackHealth(List<String> list) {
        int size = list.size();
        if (size > 0) {
            if (mLastSize == size) {
                if (list.equals(mLastList)) {
                    return;
                }
            }

            mLastSize = size;
            mLastList = list;
            if (mListener != null) {
                mListener.onHealth(isFatal, list);
            }
            return;
        }

        if (mLastSize > 0) {
            mLastSize = 0;
            if (mListener != null) {
                mListener.onHealth(isFatal, list);
            }
        }
    }

    public interface OnHealthListener {
        /**
         * 健康信息
         *
         * @param isFatal
         * @param data
         */
        void onHealth(boolean isFatal, List<String> data);
    }
}
