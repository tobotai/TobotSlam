package com.tobot.map.module.main;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.module.common.BasePopupWindow;

/**
 * @author houdeming
 * @date 2023/05/16
 */
public class TipsPopupWindow extends BasePopupWindow {
    private TextView tvTips;

    public TipsPopupWindow(Context context) {
        super(context);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.popup_tips;
    }

    @Override
    public void initView(View view) {
        tvTips = view.findViewById(R.id.tv_tips);
    }

    @Override
    public void onClick(View v) {
    }

    public void show(View view, String tips) {
        // 避免还没初始化完成就显示的情况
        try {
            tvTips.setText(tips);
            // 居上显示，避免在中间遮挡
            showAtLocation(view, Gravity.TOP, 0, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTips(String tips) {
        if (tvTips != null) {
            tvTips.setText(tips);
        }
    }
}
