package com.tobot.map.base;

import android.app.Application;

import com.tobot.map.util.CrashHandler;

/**
 * @author houdeming
 * @date 2018/3/16
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 捕获全局导常
        CrashHandler.getInstance().init(this);
    }
}
