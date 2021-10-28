package com.tobot.map.constant;

import android.annotation.SuppressLint;
import android.content.Context;

import com.tobot.map.util.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author houdeming
 * @date 2018/8/18
 */
public class BaseConstant {
    public static final String TAG = "tobotSlam";
    /**
     * 地图文件存放目录
     */
    public static final String DIRECTORY = "tobot";
    public static final String DIRECTORY_MAP_SECOND = "map";
    public static final String DIRECTORY_APK_SECOND = "upgrade";
    public static final String DIRECTORY_LOG_SECOND = "log";
    public static final String DIRECTORY_FIRMWARE_SECOND = "firmware";
    public static final String APK_NAME = "TobotSlam.apk";
    /**
     * 地图文件格式
     */
    public static final String FILE_MAP_NAME_SUFFIX = ".stcm";
    public static final String FILE_FIRMWARE_NAME_SUFFIX = ".bin";

    public static String getMapDirectory(Context context) {
        return FileUtils.getFolder(context, DIRECTORY.concat(File.separator).concat(DIRECTORY_MAP_SECOND));
    }

    public static String getApkDirectory(Context context) {
        return FileUtils.getFolder(context, DIRECTORY.concat(File.separator).concat(DIRECTORY_APK_SECOND));
    }

    public static String getLogDirectory(Context context) {
        return FileUtils.getFolder(context, DIRECTORY.concat(File.separator).concat(DIRECTORY_LOG_SECOND));
    }

    public static String getLogPath(Context context, String fileName) {
        return getLogDirectory(context).concat(File.separator).concat(fileName);
    }

    public static String getFirmwareDirectory(Context context) {
        return FileUtils.getFolder(context, DIRECTORY.concat(File.separator).concat(DIRECTORY_FIRMWARE_SECOND));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getLogFileName(Context context) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        return simpleDateFormat.format(new Date()) + ".log";
    }

    @SuppressLint("SimpleDateFormat")
    public static String getAdbLogFileName(Context context) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        return simpleDateFormat.format(new Date()) + "_adb" + ".log";
    }

    public static String getMapFileName(String number) {
        return number.concat(FILE_MAP_NAME_SUFFIX);
    }

    public static String getMapNamePath(Context context, String mapName) {
        return getMapDirectory(context).concat(File.separator).concat(mapName);
    }

    public static String getMapNumPath(Context context, String number) {
        return getMapDirectory(context).concat(File.separator).concat(number).concat(FILE_MAP_NAME_SUFFIX);
    }

    public static final String DATA_KEY = "data_key";
    public static final String NUMBER_KEY = "number_key";
    public static final String CONTENT_KEY = "content_key";
    public static final String LOOP_KEY = "loop_key";

    /**
     * 无限循环的时候默认为0
     */
    public static final int LOOP_INFINITE = 0;

    /**
     * 最大电量
     */
    public static final float BATTERY_MAX = 100.0f;

    public static final float TRY_TIME_MAX = 30.0f;
    public static final int TRY_TIME_DEFAULT = 5;

    /**
     * 最大导航速度
     */
    public static final float MAX_NAVIGATE_SPEED = 1.0f;
    /**
     * 最小导航速度
     */
    public static final float MIN_NAVIGATE_SPEED = 0.1f;
    /**
     * 最大旋转速度[0.05-2.0]
     */
    public static final float MAX_ROTATE_SPEED = 2.0f;
    /**
     * 最小旋转速度
     */
    public static final float MIN_ROTATE_SPEED = 0.1f;

    public static final int LOG_NO = 0;
    public static final int LOG_LOGCAT = 1;
    public static final int LOG_ADB = 2;

    public static final int CODE_EXIT = 100;
    public static final int CODE_UPDATE_DEVICE_DATA = 101;

    public static final String SPLIT = "，";
    public static final String SENSOR_STATUS_SPLIT = ":";
}
