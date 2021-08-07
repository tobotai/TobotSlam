package com.tobot.map.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author houdeming
 * @date 2019/5/28
 */
public class PushBean implements Parcelable {
    private int type;
    private String deviceId;
    private String content;
    private String spareParameter;

    public PushBean() {
    }

    public PushBean(int type, String deviceId, String content, String spareParameter) {
        this.type = type;
        this.deviceId = deviceId;
        this.content = content;
        this.spareParameter = spareParameter;
    }

    private PushBean(Parcel in) {
        type = in.readInt();
        deviceId = in.readString();
        content = in.readString();
        spareParameter = in.readString();
    }

    public static final Creator<PushBean> CREATOR = new Creator<PushBean>() {
        @Override
        public PushBean createFromParcel(Parcel in) {
            return new PushBean(in);
        }

        @Override
        public PushBean[] newArray(int size) {
            return new PushBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(deviceId);
        dest.writeString(content);
        dest.writeString(spareParameter);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSpareParameter() {
        return spareParameter;
    }

    public void setSpareParameter(String spareParameter) {
        this.spareParameter = spareParameter;
    }
}
