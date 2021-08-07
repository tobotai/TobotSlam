package com.tobot.map.module.main.warning;

import android.support.annotation.NonNull;

/**
 * @author houdeming
 * @date 2021/04/17
 */
public class WarningInfo implements Cloneable {
    private String type;
    private int id;
    private int count;
    private static WarningInfo warningInfo = new WarningInfo();

    public WarningInfo() {
    }

    public static WarningInfo getWarningInfo() {
        try {
            return warningInfo.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return warningInfo;
    }

    @NonNull
    @Override
    public WarningInfo clone() {
        try {
            return (WarningInfo) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new WarningInfo();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
