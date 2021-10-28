package com.tobot.map.module.set.device;

import android.support.annotation.NonNull;

/**
 * @author houdeming
 * @date 2020/4/28
 */
public class DeviceBean implements Cloneable {
    private int id;
    private String name;
    private String content;

    public DeviceBean() {
    }

    @NonNull
    @Override
    public DeviceBean clone() {
        try {
            return (DeviceBean) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DeviceBean();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
