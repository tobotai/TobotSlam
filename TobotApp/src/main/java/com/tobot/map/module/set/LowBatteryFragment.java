package com.tobot.map.module.set;

import android.view.View;
import android.widget.TextView;

import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.module.main.DataHelper;

import butterknife.BindView;

/**
 * @author houdeming
 * @date 2020/5/8
 */
public class LowBatteryFragment extends BaseFragment implements BaseBar.OnProgressChangeListener {
    @BindView(R.id.tv_current_low_battery_tips)
    TextView tvCurrentLowBattery;
    @BindView(R.id.sb_battery)
    StripSeekBar sbBattery;
    private static final float MAX = 99.0f;

    public static LowBatteryFragment newInstance() {
        return new LowBatteryFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_low_battery;
    }

    @Override
    protected void init() {
        sbBattery.setOnProgressChangeListener(this);
        int battery = DataHelper.getInstance().getLowBattery();
        tvCurrentLowBattery.setText(getString(R.string.tv_current_low_battery_tips, battery));
        // 设置battery的进度
        sbBattery.setProgress(battery / MAX);
    }

    @Override
    public void onProgressChange(View view, float progress) {
        int battery = (int) (progress * MAX);
        tvCurrentLowBattery.setText(getString(R.string.tv_current_low_battery_tips, battery));
        DataHelper.getInstance().setLowBattery(battery);
    }
}
