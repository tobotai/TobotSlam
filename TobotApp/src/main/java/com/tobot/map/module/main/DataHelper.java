package com.tobot.map.module.main;

import android.content.Context;
import android.text.TextUtils;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.entity.RecordInfo;
import com.tobot.map.util.DateTool;
import com.tobot.map.util.SharedPreferencesUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;
import com.tobot.slam.data.LocationBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author houdeming
 * @date 2018/8/18
 */
public class DataHelper {
    private static final String LOW_BATTERY_KEY = "low_battery_key";
    private static final String TRY_TIME_KEY = "try_time_key";
    private static final String LOG_KEY = "log_key";
    private static final String RELOCATION_TYPE_KEY = "relocation_type_key";
    private static final String CHASSIS_RADIUS_KEY = "chassis_radius_key";
    private static final String RELOCATION_MIN_KEY = "relocation_min_key";
    private static final String RELOCATION_SAFE_KEY = "relocation_safe_key";
    private static final String RELOCATION_AREA_RADIUS_KEY = "relocation_area_radius_key";
    private static final String CHARGE_DISTANCE_KEY = "charge_distance_key";
    private static final String CHARGE_OFFSET_KEY = "charge_offset_key";
    private static final String PRODUCT_MODEL_KEY = "product_model_key";
    private static final String TOUCH_COUNT_KEY = "touch_count_key";
    private String mMapFile, mIp;
    private int mLowBattery, mTryTime, mLogType, mRelocationQuality, mRelocationSafeValue, mRelocationType, mProductModel, mTouchCount;
    private List<RecordInfo> mWarningList;
    private float mChassisRadius, mRelocationAreaRadius, mChargeDistance, mChargeOffset;
    private boolean isLastLowBattery;

    private static class BaseDataHolder {
        private static final DataHelper INSTANCE = new DataHelper();
    }

    public static DataHelper getInstance() {
        return BaseDataHolder.INSTANCE;
    }

    public void setCurrentMapFile(String mapFile) {
        mMapFile = mapFile;
    }

    public String getCurrentMapFile() {
        return mMapFile;
    }

    public void requestNavigateCondition(Context context, NavigateConditionCallback callback) {
        // 急停按钮，请在线程中判断
        if (SlamManager.getInstance().isSystemEmergencyStop()) {
            callbackNavigateCondition(false, context.getString(R.string.emergency_stop_tips), callback);
            return;
        }

        // 刹车按钮，请在线程中判断
        if (SlamManager.getInstance().isSystemBrakeStop()) {
            callbackNavigateCondition(false, context.getString(R.string.break_stop_tips), callback);
            return;
        }

        // 如果在直充的话，不允许导航
        if (SlamManager.getInstance().isDirectCharge()) {
            callbackNavigateCondition(false, context.getString(R.string.direct_charge_to_navigate_tips), callback);
            return;
        }

        callbackNavigateCondition(true, "", callback);
    }

    public List<LocationBean> getLocationBeanList(Context context, String mapNumber) {
        List<LocationBean> list = MyDBSource.getInstance(context).queryLocationList();
        if (list != null && !list.isEmpty()) {
            for (LocationBean bean : list) {
                bean.setMapName(mapNumber);
            }
        }

        return list;
    }

    public void requestMapNameList(final Context context, final MapRequestCallback callback) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<String> data = SlamManager.getInstance().getMapList(BaseConstant.getMapDirectory(context), BaseConstant.FILE_MAP_NAME_SUFFIX);
                List<String> map = new ArrayList<>();
                if (data != null && !data.isEmpty()) {
                    for (String name : data) {
                        int index = name.lastIndexOf(BaseConstant.FILE_MAP_NAME_SUFFIX);
                        map.add(name.substring(0, index));
                    }
                }

                if (callback != null) {
                    callback.onMapList(map);
                }
            }
        });
    }

    public void requestMapFileList(final Context context, final MapRequestCallback callback) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<String> data = SlamManager.getInstance().getMapList(BaseConstant.getMapDirectory(context), BaseConstant.FILE_MAP_NAME_SUFFIX);
                if (callback != null) {
                    callback.onMapList(data);
                }
            }
        });
    }

    public void requestMapPathList(final Context context, final MapRequestCallback callback) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<String> data = SlamManager.getInstance().getMapList(BaseConstant.getMapDirectory(context), BaseConstant.FILE_MAP_NAME_SUFFIX);
                List<String> path = new ArrayList<>();
                if (data != null && !data.isEmpty()) {
                    for (String name : data) {
                        path.add(BaseConstant.getMapDirectory(context).concat(File.separator).concat(name));
                    }
                }

                if (callback != null) {
                    callback.onMapList(path);
                }
            }
        });
    }

    private void callbackNavigateCondition(boolean isCanNavigate, String reason, NavigateConditionCallback callback) {
        if (callback != null) {
            callback.onResult(isCanNavigate, reason);
        }
    }

    public interface NavigateConditionCallback {
        /**
         * 导航条件的结果
         *
         * @param isCanNavigate
         * @param reason
         */
        void onResult(boolean isCanNavigate, String reason);
    }

    public interface MapRequestCallback {
        /**
         * 地图请求的列表
         *
         * @param data
         */
        void onMapList(List<String> data);
    }

    public List<LocationBean> sortPoint(List<LocationBean> data) {
        if (data != null && !data.isEmpty()) {
            Collections.sort(data, new Comparator<LocationBean>() {
                @Override
                public int compare(LocationBean o1, LocationBean o2) {
                    if (o1 != null && o2 != null) {
                        String num1 = o1.getLocationNumber();
                        String num2 = o2.getLocationNumber();
                        boolean flag1 = !TextUtils.isEmpty(num1) && TextUtils.isDigitsOnly(num1);
                        boolean flag2 = !TextUtils.isEmpty(num2) && TextUtils.isDigitsOnly(num2);
                        if (flag1 && flag2) {
                            return Integer.compare(Integer.parseInt(num1), Integer.parseInt(num2));
                        }
                    }

                    return 0;
                }
            });
        }

        return data;
    }

    public void setLowBattery(Context context, int value) {
        mLowBattery = value;
        SharedPreferencesUtils.getInstance(context).putInt(LOW_BATTERY_KEY, value);
    }

    public int getLowBattery(Context context) {
        if (mLowBattery == 0) {
            mLowBattery = SharedPreferencesUtils.getInstance(context).getInt(LOW_BATTERY_KEY, BaseConstant.BATTERY_LOW);
        }
        return mLowBattery;
    }

    public void setLowBattery(boolean isLowBattery) {
        isLastLowBattery = isLowBattery;
    }

    public boolean isLowBattery() {
        return isLastLowBattery;
    }

    public String getTaskDetailTips(Context context, List<LocationBean> locationBeanList) {
        if (locationBeanList != null && !locationBeanList.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            String interval = "→";
            for (int i = 0, size = locationBeanList.size(); i < size; i++) {
                LocationBean bean = locationBeanList.get(i);
                String name = bean.getLocationNameChina();
                if (TextUtils.isEmpty(name)) {
                    builder.append(bean.getLocationNumber());
                } else {
                    // 有名字则显示
                    builder.append(context.getString(R.string.tv_task_point, bean.getLocationNumber(), name));
                }

                if (i < size - 1) {
                    builder.append(interval);
                }
            }

            return builder.toString();
        }

        return "";
    }

    public String getIp() {
        return mIp;
    }

    public void setIp(String ip) {
        mIp = ip;
    }

    public void setTryTime(Context context, int tryTime) {
        mTryTime = tryTime;
        SharedPreferencesUtils.getInstance(context).putInt(TRY_TIME_KEY, tryTime);
    }

    public int getTryTime(Context context) {
        if (mTryTime == 0) {
            mTryTime = SharedPreferencesUtils.getInstance(context).getInt(TRY_TIME_KEY, BaseConstant.TRY_TIME_DEFAULT);
        }
        return mTryTime;
    }

    public long getTryTimeMillis(Context context) {
        int time = getTryTime(context);
        return time == 0 ? 0 : time * 60000L;
    }

    public boolean isSlamError(String error) {
        String[] errorArray = {"Connection Time Out", "Connection Failed", "Connection Lost"};
        for (String content : errorArray) {
            if (error.contains(content)) {
                return true;
            }
        }

        return false;
    }

    public void setLogType(Context context, int type) {
        mLogType = type;
        SharedPreferencesUtils.getInstance(context).putInt(LOG_KEY, type);
    }

    public int getLogType(Context context) {
        if (mLogType == BaseConstant.LOG_NO) {
            mLogType = SharedPreferencesUtils.getInstance(context).getInt(LOG_KEY, BaseConstant.LOG_NO);
        }
        return mLogType;
    }

    public void setRelocationType(Context context, int type) {
        mRelocationType = type;
        SharedPreferencesUtils.getInstance(context).putInt(RELOCATION_TYPE_KEY, type);
    }

    public int getRelocationType(Context context) {
        if (mRelocationType == 0) {
            mRelocationType = SharedPreferencesUtils.getInstance(context).getInt(RELOCATION_TYPE_KEY, SlamCode.RELOCATION_PART);
        }
        return mRelocationType;
    }

    public void setChassisRadius(Context context, float value) {
        mChassisRadius = value;
        SharedPreferencesUtils.getInstance(context).putFloat(CHASSIS_RADIUS_KEY, value);
    }

    public float getChassisRadius(Context context) {
        if (mChassisRadius == 0) {
            mChassisRadius = SharedPreferencesUtils.getInstance(context).getFloat(CHASSIS_RADIUS_KEY, SlamCode.CHASSIS_RADIUS_DEFAULT);
        }
        return mChassisRadius;
    }

    public void setRelocationQualityMin(Context context, int value) {
        mRelocationQuality = value;
        SharedPreferencesUtils.getInstance(context).putInt(RELOCATION_MIN_KEY, value);
    }

    public int getRelocationQualityMin(Context context) {
        if (mRelocationQuality == 0) {
            mRelocationQuality = SharedPreferencesUtils.getInstance(context).getInt(RELOCATION_MIN_KEY, SlamCode.RELOCATION_QUALITY_MIN);
        }
        return mRelocationQuality;
    }

    public void setRelocationQualitySafe(Context context, int value) {
        mRelocationSafeValue = value;
        SharedPreferencesUtils.getInstance(context).putInt(RELOCATION_SAFE_KEY, value);
    }

    public int getRelocationQualitySafe(Context context) {
        if (mRelocationSafeValue == 0) {
            mRelocationSafeValue = SharedPreferencesUtils.getInstance(context).getInt(RELOCATION_SAFE_KEY, SlamCode.RELOCATION_QUALITY_SAFE);
        }
        return mRelocationSafeValue;
    }

    public void setRelocationAreaRadius(Context context, float value) {
        mRelocationAreaRadius = value;
        SharedPreferencesUtils.getInstance(context).putFloat(RELOCATION_AREA_RADIUS_KEY, value);
    }

    public float getRelocationAreaRadius(Context context) {
        if (mRelocationAreaRadius == 0) {
            mRelocationAreaRadius = SharedPreferencesUtils.getInstance(context).getFloat(RELOCATION_AREA_RADIUS_KEY, SlamCode.RELOCATION_AREA_RADIUS);
        }
        return mRelocationAreaRadius;
    }

    public void setChargeDistance(Context context, float value) {
        mChargeDistance = value;
        SharedPreferencesUtils.getInstance(context).putFloat(CHARGE_DISTANCE_KEY, value);
    }

    public float getChargeDistance(Context context) {
        if (mChargeDistance == 0) {
            mChargeDistance = SharedPreferencesUtils.getInstance(context).getFloat(CHARGE_DISTANCE_KEY, SlamCode.CHARGE_DISTANCE_X);
        }
        return mChargeDistance;
    }

    public void setChargeOffset(Context context, float value) {
        mChargeOffset = value;
        SharedPreferencesUtils.getInstance(context).putFloat(CHARGE_OFFSET_KEY, value);
    }

    public float getChargeOffset(Context context) {
        if (mChargeOffset == 0) {
            mChargeOffset = SharedPreferencesUtils.getInstance(context).getFloat(CHARGE_OFFSET_KEY, 0f);
        }
        return mChargeOffset;
    }

    public void setProductModel(Context context, int value) {
        mProductModel = value;
        SharedPreferencesUtils.getInstance(context).putInt(PRODUCT_MODEL_KEY, value);
    }

    public int getProductModel(Context context) {
        if (mProductModel == 0) {
            mProductModel = SharedPreferencesUtils.getInstance(context).getInt(PRODUCT_MODEL_KEY, SlamCode.SLAM_TYPE_DEFINED);
        }
        return mProductModel;
    }

    public void setTouchCount(Context context, int count) {
        mTouchCount = count;
        SharedPreferencesUtils.getInstance(context).putInt(TOUCH_COUNT_KEY, count);
    }

    public int getTouchCount(Context context) {
        if (mTouchCount == 0) {
            mTouchCount = SharedPreferencesUtils.getInstance(context).getInt(TOUCH_COUNT_KEY, BaseConstant.TOUCH_COUNT_DEFAULT);
        }
        return mTouchCount;
    }

    public void setWarningData(int id, String content) {
        RecordInfo info = RecordInfo.getRecordInfo();
        info.setCode(id);
        info.setContent(content);
        info.setCount(1);
        if (mWarningList == null) {
            mWarningList = new ArrayList<>();
        }

        try {
            if (!mWarningList.isEmpty()) {
                for (RecordInfo warningInfo : mWarningList) {
                    if (TextUtils.equals(info.getContent(), warningInfo.getContent()) && info.getCode() == warningInfo.getCode()) {
                        warningInfo.setCount(warningInfo.getCount() + 1);
                        return;
                    }
                }
            }

            mWarningList.add(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RecordInfo> getWarningList() {
        if (mWarningList != null && !mWarningList.isEmpty()) {
            Collections.sort(mWarningList, new Comparator<RecordInfo>() {
                @Override
                public int compare(RecordInfo o1, RecordInfo o2) {
                    if (o1 != null && o2 != null) {
                        return Integer.compare(o1.getCode(), o2.getCode());
                    }

                    return 0;
                }
            });
        }

        return mWarningList;
    }

    public void clearWarningList() {
        if (mWarningList != null && !mWarningList.isEmpty()) {
            mWarningList.clear();
        }
        mWarningList = null;
    }

    public void recordWarningList(Context context) {
        // 先删除再添加
        MyDBSource.getInstance(context).deleteAllRecordWarning();
        MyDBSource.getInstance(context).insertRecordWarningList(getWarningList());
    }

    public void recordImportantInfo(Context context, String content) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                RecordInfo info = RecordInfo.getRecordInfo();
                info.setTime(DateTool.getCurrentTimeDetail(Calendar.getInstance().getTime()));
                info.setContent(content);
                MyDBSource.getInstance(context).insertRecordInfo(info);
                // 如果超过限定的数量则替换时间最久的数据
                List<RecordInfo> infoList = MyDBSource.getInstance(context).queryRecordInfoList();
                if (infoList != null && infoList.size() > BaseConstant.MAX_RECORD_COUNT) {
                    MyDBSource.getInstance(context).deleteRecordInfo(infoList.get(0));
                }
            }
        });
    }
}
