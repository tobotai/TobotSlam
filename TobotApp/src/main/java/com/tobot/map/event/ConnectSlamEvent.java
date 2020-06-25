package com.tobot.map.event;

/**
 * @author houdeming
 * @date 2019/10/23
 */
public class ConnectSlamEvent {
    private String ip;

    public ConnectSlamEvent(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
}
