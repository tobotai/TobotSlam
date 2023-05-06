package com.tobot.map.event;

/**
 * @author houdeming
 * @date 2018/8/1
 */
public class CheckEndEvent {
    private final boolean isUpgrade;

    public CheckEndEvent(boolean isUpgrade) {
        this.isUpgrade = isUpgrade;
    }

    public boolean isUpgrade() {
        return isUpgrade;
    }
}
