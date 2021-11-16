package com.tobot.map.module.set;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.slamtec.slamware.action.MoveDirection;
import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.module.set.firmware.GetSensorDetectActivity;
import com.tobot.map.module.set.firmware.SensorInfoActivity;
import com.tobot.map.util.SystemUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;

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
    @BindView(R.id.tv_log_out_tips)
    TextView tvLogTip;
    @BindView(R.id.rb_log_no)
    RadioButton rbLogNo;
    @BindView(R.id.rb_log_logcat)
    RadioButton rbLogLogcat;
    @BindView(R.id.rb_log_adb)
    RadioButton rbLogAdb;
    private static final int TAG_ROTATE = 1;
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
        tvLogTip.setText(getString(R.string.log_file_out, BaseConstant.getLogDirectory(getActivity())));
        setSelectLogType(DataHelper.getInstance().getLogType(getActivity()));
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

    @OnClick({R.id.rb_to_left, R.id.rb_to_right, R.id.btn_send, R.id.rb_log_no, R.id.rb_log_logcat, R.id.rb_log_adb,
            R.id.rl_sensor_info, R.id.rl_get_sensor_detect_info})
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
            case R.id.rb_log_no:
                setLogType(BaseConstant.LOG_NO);
                break;
            case R.id.rb_log_logcat:
                setLogType(BaseConstant.LOG_LOGCAT);
                break;
            case R.id.rb_log_adb:
                setLogType(BaseConstant.LOG_ADB);
                break;
            case R.id.rl_sensor_info:
                startActivity(new Intent(getActivity(), SensorInfoActivity.class));
                break;
            case R.id.rl_get_sensor_detect_info:
                startActivity(new Intent(getActivity(), GetSensorDetectActivity.class));
                break;
            default:
                break;
        }
    }

    private void setProgress(float progress) {
        mBattery = (int) (progress * BaseConstant.BATTERY_MAX);
        tvCurrentLowBattery.setText(getString(R.string.tv_current_low_battery_tips, mBattery));
    }

    private void setSelectLogType(int type) {
        switch (type) {
            case BaseConstant.LOG_NO:
                rbLogNo.setChecked(true);
                break;
            case BaseConstant.LOG_LOGCAT:
                rbLogLogcat.setChecked(true);
                break;
            case BaseConstant.LOG_ADB:
                rbLogAdb.setChecked(true);
                break;
            default:
                break;
        }
    }

    private void send() {
        SystemUtils.hideKeyboard(getActivity());
        String speed = etSpeed.getText().toString().trim();
        if (TextUtils.isEmpty(speed)) {
            showToastTips(getString(R.string.rotate_speed_empty_tips));
            return;
        }

        if (TextUtils.isDigitsOnly(speed)) {
            mSpeedValue = Integer.parseInt(speed);
            ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_ROTATE));
        }
    }

    private void setLogType(int type) {
        DataHelper.getInstance().setLogType(getActivity(), type);
        if (type != BaseConstant.LOG_NO) {
            showToastTips(getString(R.string.log_select_tips));
        }
    }

    private void rotate() {
        SlamManager.getInstance().rotate(mSpeedValue, mRotateDirection);
    }

    private class TestRunnable implements Runnable {
        private int mTag;

        private TestRunnable(int tag) {
            mTag = tag;
        }

        @Override
        public void run() {
            if (mTag == TAG_ROTATE) {
                rotate();
            }
        }
    }
}
