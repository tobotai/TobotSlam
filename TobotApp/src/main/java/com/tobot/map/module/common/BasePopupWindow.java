package com.tobot.map.module.common;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.tobot.map.R;

/**
 * @author houdeming
 * @date 2020/3/15
 */
public abstract class BasePopupWindow extends PopupWindow implements View.OnClickListener {
    protected Context mContext;
    private int mPopupWidth, mPopupHeight;
    private int mX, mY;

    public BasePopupWindow(Context context) {
        super(context);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(getLayoutResId(), null, false);
        initView(view);
        setContentView(view);
        setWidth(ActionBar.LayoutParams.WRAP_CONTENT);
        setHeight(ActionBar.LayoutParams.WRAP_CONTENT);
        // 设置PopupWindow的背景
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 获取焦点，设置了可聚焦后,返回back键按下后,不会直接退出当前activity而是先退出当前的PopupWindow
        setFocusable(false);
        // 设置是否能响应点击事件,设置false后,将会阻止PopupWindow窗口里的所有点击事件
        setTouchable(true);
        // 设置允许在外点击消失
        setOutsideTouchable(false);
        // 获取自身的长宽高
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mPopupWidth = view.getMeasuredWidth();
        mPopupHeight = view.getMeasuredHeight();
    }

    public abstract int getLayoutResId();

    public abstract void initView(View view);

    public void show(View parent) {
        // 获取需要在其上方显示的控件的位置信息
        if (mX == 0 || mY == 0) {
            int[] location = new int[2];
            parent.getLocationOnScreen(location);
            mX = (location[0] + parent.getWidth() / 2) - mPopupWidth / 2;
            mY = location[1] - mPopupHeight - mContext.getResources().getDimensionPixelOffset(R.dimen.popup_margin_bottom);
        }
        // 在控件上方显示
        showAtLocation(parent, Gravity.NO_GRAVITY, mX, mY);
    }
}
