package com.tobot.map.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * @author houdeming
 * @date 2020/4/1
 */
public class RouteBean implements Parcelable, Cloneable {
    private String routeName;
    private int type;
    private String content;

    public RouteBean() {
    }

    @NonNull
    @Override
    public RouteBean clone() {
        try {
            return (RouteBean) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new RouteBean();
    }

    private RouteBean(Parcel in) {
        routeName = in.readString();
        type = in.readInt();
        content = in.readString();
    }

    public static final Creator<RouteBean> CREATOR = new Creator<RouteBean>() {
        @Override
        public RouteBean createFromParcel(Parcel in) {
            return new RouteBean(in);
        }

        @Override
        public RouteBean[] newArray(int size) {
            return new RouteBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(routeName);
        dest.writeInt(type);
        dest.writeString(content);
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
