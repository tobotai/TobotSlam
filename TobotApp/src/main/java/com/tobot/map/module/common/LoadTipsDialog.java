package com.tobot.map.module.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;

/**
 * @author houdeming
 * @date 2019/10/19
 */
public class LoadTipsDialog extends BaseDialog {
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
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_progress_width_weight) / 10.0;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            tvTips.setText(bundle.getString(DATA_KEY));
        }
    }
}
