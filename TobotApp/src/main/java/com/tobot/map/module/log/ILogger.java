package com.tobot.map.module.log;

/**
 * @author houdeming
 * @date 2021/01/28
 */
public interface ILogger {
    int VERBOSE = 2;
    int DEBUG = 3;
    int INFO = 4;
    int WARN = 5;
    int ERROR = 6;
    int ASSERT = 7;

    /**
     * 设置是否可用
     *
     * @param isLogEnable
     */
    void setLogEnable(boolean isLogEnable);

    /**
     * 保存Log
     */
    void save();

    /**
     * 关闭Log
     */
    void close();

    /**
     * v信息
     *
     * @param tag
     * @param msg
     */
    void v(String tag, String msg);

    /**
     * d信息
     *
     * @param tag
     * @param msg
     */
    void d(String tag, String msg);

    /**
     * i信息
     *
     * @param tag
     * @param msg
     */
    void i(String tag, String msg);

    /**
     * w信息
     *
     * @param tag
     * @param msg
     */
    void w(String tag, String msg);

    /**
     * e信息
     *
     * @param tag
     * @param msg
     */
    void e(String tag, String msg);
}
