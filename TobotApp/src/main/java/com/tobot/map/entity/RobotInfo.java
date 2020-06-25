package com.tobot.map.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author houdeming
 * @date 2020/5/8
 */
public class RobotInfo implements Parcelable {
    private int navigateMode;
    private int motionMode;
    private int obstacleMode;
    private int lowBattery;

    public RobotInfo() {
    }

    protected RobotInfo(Parcel in) {
        navigateMode = in.readInt();
        motionMode = in.readInt();
        obstacleMode = in.readInt();
        lowBattery = in.readInt();
    }

    public static final Creator<RobotInfo> CREATOR = new Creator<RobotInfo>() {
        @Override
        public RobotInfo createFromParcel(Parcel in) {
            return new RobotInfo(in);
        }

        @Override
        public RobotInfo[] newArray(int size) {
            return new RobotInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(navigateMode);
        dest.writeInt(motionMode);
        dest.writeInt(obstacleMode);
        dest.writeInt(lowBattery);
    }

    public int getNavigateMode() {
        return navigateMode;
    }

    public void setNavigateMode(int navigateMode) {
        this.navigateMode = navigateMode;
    }

    public int getMotionMode() {
        return motionMode;
    }

    public void setMotionMode(int motionMode) {
        this.motionMode = motionMode;
    }

    public int getObstacleMode() {
        return obstacleMode;
    }

    public void setObstacleMode(int obstacleMode) {
        this.obstacleMode = obstacleMode;
    }

    public int getLowBattery() {
        return lowBattery;
    }

    public void setLowBattery(int lowBattery) {
        this.lowBattery = lowBattery;
    }
}
