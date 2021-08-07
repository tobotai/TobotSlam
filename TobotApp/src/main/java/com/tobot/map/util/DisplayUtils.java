package com.tobot.map.util;

import android.content.Context;

/**
 * @author houdeming
 * @date 2018/4/13
 */
public class DisplayUtils {

    public static int getScreenHeightPixels(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidthPixels(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
