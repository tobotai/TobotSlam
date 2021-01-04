package com.tobot.map.module.set;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.slamtec.slamware.action.MoveDirection;
import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2020/5/6
 */
public class TestFragment extends BaseFragment implements BaseBar.OnSeekBarChangeListener {
    @BindView(R.id.tv_low_battery_tips)
    TextView tvCurrentLowBattery;
    @BindView(R.id.sb_battery)
    StripSeekBar sbBattery;
    @BindView(R.id.et_speed)
    EditText etSpeed;
    private int mBattery, mSpeedValue;
    private MoveDirection mRotateDirection = MoveDirection.TURN_LEFT;

    public static TestFragment newInstance() {
        return new TestFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_test;
    }

    @Override
    protected void init() {
        sbBattery.setOnSeekBarChangeListener(this);
        int battery = DataHelper.getInstance().getLowBattery();
        tvCurrentLowBattery.setText(getString(R.string.tv_current_low_battery_tips, battery));
        // 设置battery的进度
        sbBattery.setProgress(battery / BaseConstant.BATTERY_MAX);
    }

    @Override
    public void onSeekBarStart(View view) {
    }

    @Override
    public void onProgressChange(View view, float progress) {
        setProgress(progress);
    }

    @Override
    public void onSeekBarStop(View view, float progress) {
        setProgress(progress);
        DataHelper.getInstance().setLowBattery(mBattery);
        showToastTips(getString(R.string.set_success_tips));
    }

    @OnClick({R.id.rb_to_left, R.id.rb_to_right, R.id.btn_send})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.rb_to_left:
                mRotateDirection = MoveDirection.TURN_LEFT;
                break;
            case R.id.rb_to_right:
                mRotateDirection = MoveDirection.TURN_RIGHT;
                break;
            case R.id.btn_send:
                send();
                break;
            default:
                break;
        }
    }

    private void setProgress(float progress) {
        mBattery = (int) (progress * BaseConstant.BATTERY_MAX);
        tvCurrentLowBattery.setText(getString(R.string.tv_current_low_battery_tips, mBattery));
    }

    private void send() {
        String speed = etSpeed.getText().toString().trim();
        if (TextUtils.isEmpty(speed)) {
            showToastTips(getString(R.string.rotate_speed_empty_tips));
            return;
        }

        if (TextUtils.isDigitsOnly(speed)) {
            mSpeedValue = Integer.parseInt(speed);
            ThreadPoolManager.getInstance().execute(new TestRunnable(new WeakReference<>(this)));
        }
    }

    private void rotate() {
        SlamManager.getInstance().rotate(mSpeedValue, mRotateDirection);
    }

    private static class TestRunnable implements Runnable {
        private TestFragment mFragment;

        private TestRunnable(WeakReference<TestFragment> reference) {
            mFragment = reference.get();
        }

        @Override
        public void run() {
            if (mFragment != null) {
                mFragment.rotate();
            }
        }
    }
}
