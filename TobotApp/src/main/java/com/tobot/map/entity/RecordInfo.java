package com.tobot.map.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * @author houdeming
 * @date 2021/04/17
 */
public class RecordInfo implements Parcelable, Cloneable {
    private int type;
    private int code;
    private int count;
    private String time;
    private String content;
    private static final RecordInfo RECORD_INFO = new RecordInfo();

    public RecordInfo() {
    }

    protected RecordInfo(Parcel in) {
        type = in.readInt();
        code = in.readInt();
        count = in.readInt();
        time = in.readString();
        content = in.readString();
    }

    public static final Creator<RecordInfo> CREATOR = new Creator<RecordInfo>() {
        @Override
        public RecordInfo createFromParcel(Parcel in) {
            return new RecordInfo(in);
        }

        @Override
        public RecordInfo[] newArray(int size) {
            return new RecordInfo[size];
        }
    };

    public static RecordInfo getRecordInfo() {
        try {
            return RECORD_INFO.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RECORD_INFO;
    }

    @NonNull
    @Override
    public RecordInfo clone() {
        try {
            return (RecordInfo) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new RecordInfo();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(code);
        dest.writeInt(count);
        dest.writeString(time);
        dest.writeString(content);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
