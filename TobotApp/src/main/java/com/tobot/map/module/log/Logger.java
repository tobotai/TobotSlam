package com.tobot.map.module.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author houdeming
 * @date 2021/01/28
 */
public final class Logger {
    private static HashMap<String, ILogger> loggerHashMap = new HashMap<>();
    private static boolean isLogEnable = true;

    private Logger() {
    }

    public static boolean addLogger(ILogger logger) {
        if (logger == null) {
            return false;
        }

        String loggerName = logger.getClass().getName();
        boolean isSuccess = false;
        if (!loggerHashMap.containsKey(loggerName)) {
            loggerHashMap.put(loggerName, logger);
            isSuccess = true;
        }

        return isSuccess;
    }

    public static void removeLogger(ILogger logger) {
        if (logger == null) {
            return;
        }

        String loggerName = logger.getClass().getName();
        if (loggerHashMap.containsKey(loggerName)) {
            try {
                logger.close();
                // 退出前先保存
                logger.save();
                loggerHashMap.remove(loggerName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setLogEnable(boolean isLogEnable) {
        Logger.isLogEnable = isLogEnable;
        for (Map.Entry<String, ILogger> entry : loggerHashMap.entrySet()) {
            ILogger logger = entry.getValue();
            if (logger != null) {
                logger.setLogEnable(isLogEnable);
            }
        }
    }

    public static void save() {
        if (loggerHashMap == null || loggerHashMap.size() == 0) {
            return;
        }

        for (String key : loggerHashMap.keySet()) {
            save(loggerHashMap.get(key));
        }
    }

    public static void save(ILogger logger) {
        if (logger != null) {
            logger.save();
        }
    }

    public static void close() {
        if (loggerHashMap == null || loggerHashMap.size() == 0) {
            return;
        }

        for (String key : loggerHashMap.keySet()) {
            close(loggerHashMap.get(key));
        }
    }

    public static void close(ILogger logger) {
        if (logger != null) {
            logger.close();
        }
    }

    public static void v(String tag, String message) {
        printLogger(ILogger.VERBOSE, tag, message);
    }

    public static void v(String tag, String message, Throwable tr) {
        printLogger(ILogger.VERBOSE, tag, message + '\n' + getStackTraceString(tr));
    }

    public static void d(String tag, String message) {
        printLogger(ILogger.DEBUG, tag, message);
    }

    public static void d(String tag, String message, Throwable tr) {
        printLogger(ILogger.DEBUG, tag, message + '\n' + getStackTraceString(tr));
    }

    public static void i(String tag, String message) {
        printLogger(ILogger.INFO, tag, message);
    }

    public static void i(String tag, String message, Throwable tr) {
        printLogger(ILogger.INFO, tag, message + '\n' + getStackTraceString(tr));
    }

    public static void w(String tag, String message) {
        printLogger(ILogger.WARN, tag, message);
    }

    public static void w(String tag, String message, Throwable tr) {
        printLogger(ILogger.WARN, tag, message + '\n' + getStackTraceString(tr));
    }

    public static void e(String tag, String message) {
        printLogger(ILogger.ERROR, tag, message);
    }

    public static void e(String tag, String message, Throwable tr) {
        printLogger(ILogger.ERROR, tag, message + '\n' + getStackTraceString(tr));
    }

    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private static void printLogger(int priority, String tag, String message) {
        if (!isLogEnable) {
            return;
        }

        for (Map.Entry<String, ILogger> entry : loggerHashMap.entrySet()) {
            ILogger logger = entry.getValue();
            if (logger != null) {
                printLogger(logger, priority, tag, message);
            }
        }
    }

    private static void printLogger(ILogger logger, int priority, String tag, String message) {
        switch (priority) {
            case ILogger.VERBOSE:
                logger.v(tag, message);
                break;
            case ILogger.DEBUG:
                logger.d(tag, message);
                break;
            case ILogger.INFO:
                logger.i(tag, message);
                break;
            case ILogger.WARN:
                logger.w(tag, message);
                break;
            case ILogger.ERROR:
                logger.e(tag, message);
                break;
            default:
                break;
        }
    }
}
