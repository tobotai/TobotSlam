package com.tobot.map.base;

import android.app.Application;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.FileLogger;
import com.tobot.map.module.log.LogcatLogger;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.CrashHandler;

/**
 * @author houdeming
 * @date 2018/3/16
 */
public class BaseApplication extends Application implements CrashHandler.OnUncaughtExceptionListener {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.addLogger(new LogcatLogger(this));
        if (DataHelper.getInstance().getLogType(this) == BaseConstant.LOG_LOGCAT) {
            Logger.addLogger(new FileLogger(this, BaseConstant.getLogDirectory(this), BaseConstant.getLogFileName(this)));
        }
        Logger.i(BaseConstant.TAG, "onCreate()");
        // 捕获全局导常
        CrashHandler.getInstance().init(this, false, this);
    }

    @Override
    public void onUncaughtException(String error) {
        Logger.i(BaseConstant.TAG, "error=" + error);
    }
}
