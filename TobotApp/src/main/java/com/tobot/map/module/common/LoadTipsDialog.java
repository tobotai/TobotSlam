package com.tobot.map.module.common;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseV4Dialog;

/**
 * @author houdeming
 * @date 2019/10/19
 */
public class LoadTipsDialog extends BaseV4Dialog {
    private TextView tvTips;

    public static LoadTipsDialog newInstance(String tips) {
        LoadTipsDialog dialog = new LoadTipsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_KEY, tips);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_load_tips;
    }

    @Override
    protected void initView(View view) {
        tvTips = view.findViewById(R.id.tv_tips);
    }

    @Override
    protected boolean isCanCancelByBack() {
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            tvTips.setText(bundle.getString(DATA_KEY));
        }
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_progress_width_weight) / 10.0;
    }
}
