package com.tobot.map.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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
}
