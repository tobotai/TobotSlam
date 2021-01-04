package com.tobot.map.entity;

/**
 * @author houdeming
 * @date 2020/12/30
 */
public class SetBean {
    private int id;
    private String name;

    public SetBean(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
