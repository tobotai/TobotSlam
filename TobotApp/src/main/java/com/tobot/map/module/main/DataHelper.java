package com.tobot.map.module.main;

import android.content.Context;
import android.text.TextUtils;

import com.tobot.map.R;
import com.tobot.map.base.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.slam.SlamManager;
import com.tobot.slam.data.LocationBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author houdeming
 * @date 2018/8/18
 */
public class DataHelper {
    private String mMapName;
    private int mLowBattery;
    private String mIp;

    private static class BaseDataHolder {
        private static final DataHelper INSTANCE = new DataHelper();
    }

    public static DataHelper getInstance() {
        return BaseDataHolder.INSTANCE;
    }

    public void setCurrentMapName(String number) {
        mMapName = number;
    }

    public String getCurrentMapName() {
        return mMapName;
    }

    public void requestNavigateCondition(Context context, NavigateConditionCallBack callBack) {
        // 急停按钮，请在线程中判断
        if (SlamManager.getInstance().isSystemEmergencyStop()) {
            callBackNavigateCondition(false, context.getString(R.string.emergency_stop_tips), callBack);
            return;
        }
        // 刹车按钮，请在线程中判断
        if (SlamManager.getInstance().isSystemBrakeStop()) {
            callBackNavigateCondition(false, context.getString(R.string.break_stop_tips), callBack);
            return;
        }
        // 如果在直充的话，不允许导航
        if (isDirectCharge()) {
            callBackNavigateCondition(false, context.getString(R.string.direct_charge_to_navigate_tips), callBack);
            return;
        }
        callBackNavigateCondition(true, "", callBack);
    }

    public List<LocationBean> getLocationBeanList(Context context, String mapNumber) {
        List<LocationBean> list = MyDBSource.getInstance(context).queryLocation();
        if (list != null && !list.isEmpty()) {
            for (LocationBean bean : list) {
                bean.setMapName(mapNumber);
            }
        }
        return list;
    }

    public void requestMapNumberList(final Context context, final MapRequestCallBack callBack) {
        ExecutorService mExecutor = Executors.newCachedThreadPool();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<String> data = SlamManager.getInstance().getMapList(BaseConstant.getMapDirectory(context), BaseConstant.FILE_NAME_SUFFIX);
                List<String> map = new ArrayList<>();
                if (data != null && !data.isEmpty()) {
                    for (String name : data) {
                        int index = name.lastIndexOf(BaseConstant.FILE_NAME_SUFFIX);
                        map.add(name.substring(0, index));
                    }
                }
                if (callBack != null) {
                    callBack.onMapList(map);
                }
            }
        });
    }

    public void requestMapNameList(final Context context, final MapRequestCallBack callBack) {
        ExecutorService mExecutor = Executors.newCachedThreadPool();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<String> data = SlamManager.getInstance().getMapList(BaseConstant.getMapDirectory(context), BaseConstant.FILE_NAME_SUFFIX);
                if (callBack != null) {
                    callBack.onMapList(data);
                }
            }
        });
    }

    public void requestMapPathList(final Context context, final MapRequestCallBack callBack) {
        ExecutorService mExecutor = Executors.newCachedThreadPool();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<String> data = SlamManager.getInstance().getMapList(BaseConstant.getMapDirectory(context), BaseConstant.FILE_NAME_SUFFIX);
                List<String> path = new ArrayList<>();
                if (data != null && !data.isEmpty()) {
                    for (String name : data) {
                        path.add(BaseConstant.getMapDirectory(context).concat(File.separator).concat(name));
                    }
                }
                if (callBack != null) {
                    callBack.onMapList(path);
                }
            }
        });
    }

    /**
     * 是否在直充
     *
     * @return
     */
    private boolean isDirectCharge() {
        // 是否在充电桩上（在充电桩上并且充着电的情况）
        if (SlamManager.getInstance().isDockingStatus()) {
            return false;
        }
        // 如果不在充电桩上的话，要考虑直充的情况
        return SlamManager.getInstance().isBatteryCharging();
    }

    private void callBackNavigateCondition(boolean isCanNavigate, String reason, NavigateConditionCallBack callBack) {
        if (callBack != null) {
            callBack.onResult(isCanNavigate, reason);
        }
    }

    public interface NavigateConditionCallBack {
        /**
         * 导航条件的结果
         *
         * @param isCanNavigate
         * @param reason
         */
        void onResult(boolean isCanNavigate, String reason);
    }

    public interface MapRequestCallBack {
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

    public void setLowBattery(int battery) {
        mLowBattery = battery;
    }

    public int getLowBattery() {
        return mLowBattery;
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
}
