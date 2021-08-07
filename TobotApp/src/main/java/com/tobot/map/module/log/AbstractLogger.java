package com.tobot.map.module.log;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author houdeming
 * @date 2021/01/28
 */
public abstract class AbstractLogger implements ILogger {

    protected StackTraceElement getStackTraceElement(int index) {
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        return traceElements[index];
    }

    protected String formatLogMsg(String msg, String fileName, int lineNumber, String methodName) {
        return "(" + fileName + ":" + lineNumber + ") " + methodName + "() " + msg;
    }

    /**
     * 打印log的格式
     *
     * @param processTag
     * @param packageName
     * @param priorityTag
     * @param tag
     * @param message
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    protected String getPrintLog(String processTag, String packageName, String priorityTag, String tag, String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time = simpleDateFormat.format(new Date());
        return time + " " + processTag + "/" + packageName + " " + priorityTag + "/" + tag + ": " + message + "\n";
    }

    protected String getPriorityTag(int priority) {
        String priorityTag = "V";
        switch (priority) {
            case ILogger.VERBOSE:
                priorityTag = "V";
                break;
            case ILogger.DEBUG:
                priorityTag = "D";
                break;
            case ILogger.INFO:
                priorityTag = "I";
                break;
            case ILogger.WARN:
                priorityTag = "W";
                break;
            case ILogger.ERROR:
                priorityTag = "E";
                break;
            default:
                break;
        }

        return priorityTag;
    }
}
