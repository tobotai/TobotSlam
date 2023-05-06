package com.tobot.map.base;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * @author houdeming
 * @date 2018/4/13
 */
public abstract class BaseWindow {
    protected Context mContext;
    private final LayoutInflater mInflater;
    private final WindowManager mWindowManager;
    private final LayoutParams mLayoutParams;
    private View mView;

    public BaseWindow(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // 设置布局参数
        mLayoutParams = new LayoutParams();
        // 设置window TYPE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = LayoutParams.TYPE_PHONE;
        }
        // 设置图片格式，效果位背景透明
        mLayoutParams.format = PixelFormat.RGBA_8888;
        // 设置Window flag
        mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = getGravity();
        // 以屏幕右下角为原点，设置X y初始值
        mLayoutParams.x = getLayoutOriginalX();
        mLayoutParams.y = getLayoutOriginalY();
        // 设置悬浮窗口长宽数据
        mLayoutParams.width = isWidthMatchParent() ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = isHeightMatchParent() ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
    }

    public void addView() {
        try {
            if (mView == null) {
                mView = getView(mInflater);
                mWindowManager.addView(mView, mLayoutParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeView() {
        try {
            if (mWindowManager != null && mView != null) {
                mWindowManager.removeView(mView);
                mView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取view
     *
     * @param inflater
     * @return
     */
    public abstract View getView(LayoutInflater inflater);

    /**
     * 获取位置
     *
     * @return
     */
    public abstract int getGravity();

    /**
     * 是否宽全包裹
     *
     * @return
     */
    public abstract boolean isWidthMatchParent();

    /**
     * 是否高全包裹
     *
     * @return
     */
    public abstract boolean isHeightMatchParent();

    /**
     * 获取X轴的初始值
     *
     * @return
     */
    public abstract int getLayoutOriginalX();

    /**
     * 获取Y轴的初始值
     *
     * @return
     */
    public abstract int getLayoutOriginalY();
}
