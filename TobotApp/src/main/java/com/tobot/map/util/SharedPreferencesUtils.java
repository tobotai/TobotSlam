package com.tobot.map.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * @author houdeming
 * @date 2018/4/27
 */
public class SharedPreferencesUtils {
    private static SharedPreferencesUtils sInstance = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    private SharedPreferencesUtils(Context context) {
        sharedPreferences = context.getSharedPreferences("sp_data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public synchronized static SharedPreferencesUtils getInstance(Context context) {
        if (context == null) {
            return sInstance;
        }

        if (sInstance == null) {
            synchronized (SharedPreferencesUtils.class) {
                if (sInstance == null) {
                    sInstance = new SharedPreferencesUtils(context.getApplicationContext());
                }
            }
        }

        return sInstance;
    }

    public boolean putBoolean(String name, boolean value) {
        if (editor == null || TextUtils.isEmpty(name)) {
            return false;
        }

        editor.putBoolean(name, value);
        editor.commit();
        return true;
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        if (sharedPreferences == null || TextUtils.isEmpty(name)) {
            return defaultValue;
        }

        return sharedPreferences.getBoolean(name, defaultValue);
    }

    public boolean putString(String name, String value) {
        if (editor == null || TextUtils.isEmpty(name)) {
            return false;
        }

        editor.putString(name, value);
        editor.commit();
        return true;
    }

    public String getString(String name, String defaultValue) {
        if (sharedPreferences == null || TextUtils.isEmpty(name)) {
            return defaultValue;
        }

        return sharedPreferences.getString(name, defaultValue);
    }

    public boolean putLong(String name, long value) {
        if (editor == null || TextUtils.isEmpty(name)) {
            return false;
        }

        editor.putLong(name, value);
        editor.commit();
        return true;
    }

    public long getLong(String name, long defaultValue) {
        if (sharedPreferences == null || TextUtils.isEmpty(name)) {
            return defaultValue;
        }

        return sharedPreferences.getLong(name, defaultValue);
    }

    public boolean putInt(String name, int value) {
        if (editor == null || TextUtils.isEmpty(name)) {
            return false;
        }

        editor.putInt(name, value);
        editor.commit();
        return true;
    }

    public int getInt(String name, int defaultValue) {
        if (sharedPreferences == null || TextUtils.isEmpty(name)) {
            return defaultValue;
        }

        return sharedPreferences.getInt(name, defaultValue);
    }

    public boolean putFloat(String name, float value) {
        if (editor == null || TextUtils.isEmpty(name)) {
            return false;
        }

        editor.putFloat(name, value);
        editor.commit();
        return true;
    }

    public float getFloat(String name, float defaultValue) {
        if (sharedPreferences == null || TextUtils.isEmpty(name)) {
            return defaultValue;
        }

        return sharedPreferences.getFloat(name, defaultValue);
    }

    public boolean removeValue(String key) {
        if (TextUtils.isEmpty(key) || sharedPreferences == null || editor == null) {
            return false;
        }

        if (!sharedPreferences.contains(key)) {
            return false;
        }

        editor.remove(key);
        editor.commit();
        return true;
    }

    public boolean clearData() {
        if (editor == null) {
            return false;
        }

        editor.clear();
        editor.commit();
        return true;
    }
}
