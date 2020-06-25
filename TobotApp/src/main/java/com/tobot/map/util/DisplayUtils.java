package com.tobot.map.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author houdeming
 * @date 2018/4/13
 */
public class DisplayUtils {

    public static int getScreenHeightPixels(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    public static int getScreenWidthPixels(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getRealMetrics(metrics);
        return metrics;
    }
}
