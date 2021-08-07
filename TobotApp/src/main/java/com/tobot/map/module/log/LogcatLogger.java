package com.tobot.map.module.log;

import android.content.Context;
import android.util.Log;

/**
 * @author houdeming
 * @date 2021/01/28
 */
public class LogcatLogger extends AbstractLogger {
    private boolean isLogEnable = true;

    public LogcatLogger(Context context) {
    }

    @Override
    public void setLogEnable(boolean isLogEnable) {
        this.isLogEnable = isLogEnable;
    }

    @Override
    public void save() {
    }

    @Override
    public void close() {
        isLogEnable = false;
    }

    @Override
    public void v(String tag, String msg) {
        if (!isLogEnable) {
            return;
        }

        StackTraceElement element = getStackTraceElement(7);
        Log.v(tag, formatLogMsg(msg, element.getFileName(), element.getLineNumber(), element.getMethodName()));
    }

    @Override
    public void d(String tag, String msg) {
        if (!isLogEnable) {
            return;
        }

        StackTraceElement element = getStackTraceElement(7);
        Log.d(tag, formatLogMsg(msg, element.getFileName(), element.getLineNumber(), element.getMethodName()));
    }

    @Override
    public void i(String tag, String msg) {
        if (!isLogEnable) {
            return;
        }

        StackTraceElement element = getStackTraceElement(7);
        Log.i(tag, formatLogMsg(msg, element.getFileName(), element.getLineNumber(), element.getMethodName()));
    }

    @Override
    public void w(String tag, String msg) {
        if (!isLogEnable) {
            return;
        }

        StackTraceElement element = getStackTraceElement(7);
        Log.w(tag, formatLogMsg(msg, element.getFileName(), element.getLineNumber(), element.getMethodName()));
    }

    @Override
    public void e(String tag, String msg) {
        if (!isLogEnable) {
            return;
        }

        StackTraceElement element = getStackTraceElement(7);
        Log.e(tag, formatLogMsg(msg, element.getFileName(), element.getLineNumber(), element.getMethodName()));
    }
}
