package com.tobot.map.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;

/**
 * @author houdeming
 * @date 2019/11/4
 */
public class SystemUtils {

    protected void hideKeyBoard(Activity activity) {
        try {
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null && manager.isActive()) {
                View view = activity.getCurrentFocus();
                if (view != null) {
                    manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求屏幕信息
     *
     * @param context
     */
    public static void requestDisplayInfo(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        // 屏幕宽、高、像素
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        float density = metrics.density;
        Logger.i(BaseConstant.TAG, "screenWidth=" + screenWidth + ",screenHeight=" + screenHeight + ",density=" + density);
    }

    public static int getDisplayWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
}
