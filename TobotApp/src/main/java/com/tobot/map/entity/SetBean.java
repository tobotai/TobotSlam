package com.tobot.map.entity;

import android.support.annotation.NonNull;

/**
 * @author houdeming
 * @date 2020/12/30
 */
public class SetBean implements Cloneable {
    private int id;
    private String name;

    public SetBean() {
    }

    public SetBean(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    @Override
    public SetBean clone() {
        try {
            return (SetBean) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new SetBean();
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
}
