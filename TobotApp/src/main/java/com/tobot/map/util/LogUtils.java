package com.tobot.map.util;

import android.util.Log;

/**
 * @author houdeming
 * @date 2018/4/13
 */
public class LogUtils {
    private static final String TAG = "tobotSlam";
    private static boolean isDebug = true;

    private LogUtils() {
    }

    public static void setLog(boolean isOutLog) {
        isDebug = isOutLog;
    }

    public static void i(String msg) {
        if (isDebug) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String msg, Throwable throwable) {
        if (isDebug) {
            Log.i(TAG, msg, throwable);
        }
    }

    public static void e(String msg) {
        if (isDebug) {
            Log.e(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (isDebug) {
            Log.v(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (isDebug) {
            Log.w(TAG, msg);
        }
    }
}
