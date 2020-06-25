package com.tobot.map.util;

import android.util.Log;

import com.tobot.map.BuildConfig;

/**
 * @author houdeming
 * @date 2018/4/13
 */
public class LogUtils {
    private static final String TAG = "tobotSlam";

    private LogUtils() {
    }

    public static void i(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String msg, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, msg, throwable);
        }
    }

    public static void e(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, msg);
        }
    }
}
