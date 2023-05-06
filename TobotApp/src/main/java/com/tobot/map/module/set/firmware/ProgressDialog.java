package com.tobot.map.module.set.firmware;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.tobot.bar.progressbar.StripProgressBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;

import java.util.Locale;

/**
 * @author houdeming
 * @date 2021/05/10
 */
public class ProgressDialog extends BaseDialog {
    private TextView tvProgress;
    private StripProgressBar progressBar;
    private TextView tvTips;

    public static ProgressDialog newInstance(String tips) {
        ProgressDialog dialog = new ProgressDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_KEY, tips);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_progress_tips;
    }

    @Override
    protected void initView(View view) {
        tvProgress = view.findViewById(R.id.tv_progress);
        progressBar = view.findViewById(R.id.pb_progress);
        tvTips = view.findViewById(R.id.tv_tips);
        updateTips(0);
    }

    @Override
    protected boolean isCanCancelByBack() {
        return false;
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            tvTips.setText(bundle.getString(DATA_KEY));
        }
    }

    public void updateTips(int progress) {
        if (tvProgress != null) {
            tvProgress.setText(String.format(Locale.CHINESE, "%d%%", progress));
            progressBar.setProgress(progress / 100.0f);
        }
    }
}
