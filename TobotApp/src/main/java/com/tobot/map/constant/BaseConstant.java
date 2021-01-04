package com.tobot.map.constant;

import android.content.Context;

import com.tobot.map.util.FileUtils;

import java.io.File;

/**
 * @author houdeming
 * @date 2018/8/18
 */
public class BaseConstant {
    /**
     * 地图文件存放目录
     */
    public static final String DIRECTORY = "tobot";
    public static final String DIRECTORY_MAP_SECOND = "map";
    /**
     * 地图文件格式
     */
    public static final String FILE_NAME_SUFFIX = ".stcm";

    public static String getMapDirectory(Context context) {
        String directory = DIRECTORY;
        directory = directory.concat(File.separator).concat(DIRECTORY_MAP_SECOND);
        return FileUtils.getFolder(context, directory);
    }

    public static String getMapFileName(String number) {
        return number.concat(FILE_NAME_SUFFIX);
    }

    public static String getMapNamePath(Context context, String mapName) {
        return getMapDirectory(context).concat(File.separator).concat(mapName);
    }

    public static String getMapNumPath(Context context, String number) {
        return getMapDirectory(context).concat(File.separator).concat(number).concat(FILE_NAME_SUFFIX);
    }

    /**
     * 定位质量的最小值（根据实际情况调整，定位质量低于40超过3秒就停了）
     */
    public static final int LOCALIZATION_QUALITY_MIN = 43;
    public static final String DATA_KEY = "data_key";
    public static final String NUMBER_KEY = "number_key";
    public static final String LOOP_KEY = "loop_key";
    /**
     * 无限循环的时候默认为0
     */
    public static final int LOOP_INFINITE = 0;
    
    public static final float TRY_TIME_MAX = 30.0f;

    /**
     * 最大电量
     */
    public static final float BATTERY_MAX = 100.0f;
}
