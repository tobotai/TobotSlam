package com.tobot.map.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author houdeming
 * @date 2020/2/24
 */
public class ToastUtils {
    private static ToastUtils sInstance;
    private static Toast mToast;

    @SuppressLint("ShowToast")
    private ToastUtils(Context context) {
        mToast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
        // 居中显示
        mToast.setGravity(Gravity.CENTER, 0, 0);

//        try {
//            LinearLayout linearLayout = (LinearLayout) mToast.getView();
//            if (linearLayout != null) {
//                TextView textView = (TextView) linearLayout.getChildAt(0);
//                textView.setTextSize(context.getResources().getDimension(R.dimen.tv_toast_size));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static ToastUtils getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ToastUtils.class) {
                if (sInstance == null) {
                    sInstance = new ToastUtils(context.getApplicationContext());
                }
            }
        }

        return sInstance;
    }

    public void show(int resId) {
        if (mToast != null) {
            mToast.setText(resId);
            mToast.show();
        }
    }

    public void show(String content) {
        if (mToast != null) {
            mToast.setText(content);
            mToast.show();
        }
    }

    public void cancel() {
        // 调用cancel()之后可能就show不出来了
        if (mToast != null) {
            mToast.cancel();
        }
    }
}
