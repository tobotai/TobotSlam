package com.tobot.map.base;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.tobot.map.util.DisplayUtils;
import com.tobot.map.util.LogUtils;

/**
 * @author houdeming
 * @date 2018/4/20
 */
public abstract class BaseDialog extends DialogFragment {
    protected static final String DATA_KEY = "data_key";
    private OnDialogBackEventListener mOnDialogBackEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 去掉dialog的标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(getLayoutId(), null);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 解决因为系统定制不同问题造成的dialog大小显示的问题
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 只点击弹框才消失，点击屏幕以外的地方不消失
//            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            // 监听返回键
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                        LogUtils.i("dialog back event");
                        if (mOnDialogBackEventListener != null) {
                            mOnDialogBackEventListener.onDialogBackEvent();
                        }
                    }
                    return false;
                }
            });
            Window window = dialog.getWindow();
            if (window != null) {
                // 去掉dialog黑框
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setGravity(Gravity.CENTER);
                double percentage = getScreenWidthPercentage();
                int width = (int) (DisplayUtils.getScreenWidthPixels(getActivity()) * percentage);
                // 如果屏幕宽的百分比不是100%的话，再考虑横竖屏的问题
                if (percentage != 1) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        width = (int) (DisplayUtils.getScreenHeightPixels(getActivity()) * percentage);
                    }
                }
                // 如果宽是全屏的话，则高也默认全屏
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (percentage == 1) {
                    height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                window.setLayout(width, height);
            }
        }
    }

    /**
     * 获取资源ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化
     *
     * @param view
     */
    protected abstract void initView(View view);

    /**
     * 获取屏幕宽的百分比
     *
     * @return
     */
    protected abstract double getScreenWidthPercentage();

    protected void setOnDialogBackEventListener(OnDialogBackEventListener listener) {
        mOnDialogBackEventListener = listener;
    }
}
