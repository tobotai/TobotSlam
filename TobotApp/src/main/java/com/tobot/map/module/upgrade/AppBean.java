package com.tobot.map.module.upgrade;

/**
 * @author houdeming
 * @date 2018/7/30
 */
public class AppBean {
    private String appName;
    private String appPackageName;
    private int appCode;
    private String url;

    public AppBean() {
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public int getAppCode() {
        return appCode;
    }

    public void setAppCode(int appCode) {
        this.appCode = appCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
