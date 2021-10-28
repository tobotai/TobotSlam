package com.tobot.map.module.set.firmware;

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
public class UpgradeProgressDialog extends BaseDialog {
    private TextView tvProgress;
    private StripProgressBar progressBar;

    public static UpgradeProgressDialog newInstance() {
        return new UpgradeProgressDialog();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_upgrade_progress_tips;
    }

    @Override
    protected void initView(View view) {
        tvProgress = view.findViewById(R.id.tv_progress);
        progressBar = view.findViewById(R.id.pb_progress);
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

    public void updateTips(int progress) {
        if (tvProgress != null) {
            tvProgress.setText(String.format(Locale.CHINESE, "%d%%", progress));
            progressBar.setProgress(progress / 100.0f);
        }
    }
}
