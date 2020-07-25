package com.tobot.map.base;

import android.app.Application;

import com.tobot.map.BuildConfig;
import com.tobot.map.util.CrashHandler;
import com.tobot.map.util.LogUtils;

/**
 * @author houdeming
 * @date 2018/3/16
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.setLog(BuildConfig.LOG_DEBUG);
        // 捕获全局导常
        CrashHandler.getInstance().init(this);
    }
}
