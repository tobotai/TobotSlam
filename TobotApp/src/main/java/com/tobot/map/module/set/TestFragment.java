package com.tobot.map.module.set;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.firmware.IdType;

import java.util.List;

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
    @BindView(R.id.btn_sensor_sonar_count)
    Button btnSonarCount;
    @BindView(R.id.btn_sensor_cliff_count)
    Button btnCliffCount;
    @BindView(R.id.btn_sensor_bumper_count)
    Button btnBumperCount;
    @BindView(R.id.btn_sensor_sonar_status)
    Button btnSonarStatus;
    @BindView(R.id.btn_sensor_cliff_status)
    Button btnCliffStatus;
    @BindView(R.id.btn_sensor_bumper_status)
    Button btnBumperStatus;
    private static final int TAG_SONAR_COUNT = 0;
    private static final int TAG_CLIFF_COUNT = 1;
    private static final int TAG_BUMPER_COUNT = 2;
    private static final int TAG_SONAR_STATUS = 3;
    private static final int TAG_CLIFF_STATUS = 4;
    private static final int TAG_BUMPER_STATUS = 5;
    private static final int TAG_ROTATE = 6;
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
        String unKnown = getString(R.string.unknown);
        btnSonarCount.setText(getString(R.string.sensor_sonar, unKnown));
        btnCliffCount.setText(getString(R.string.sensor_cliff, unKnown));
        btnBumperCount.setText(getString(R.string.sensor_bumper, unKnown));
        btnSonarStatus.setText(getString(R.string.sensor_sonar, unKnown));
        btnCliffStatus.setText(getString(R.string.sensor_cliff, unKnown));
        btnBumperStatus.setText(getString(R.string.sensor_bumper, unKnown));
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

    @OnClick({R.id.rb_to_left, R.id.rb_to_right, R.id.btn_send, R.id.rb_log_no, R.id.rb_log_logcat, R.id.rb_log_adb, R.id.btn_sensor_sonar_count,
            R.id.btn_sensor_cliff_count, R.id.btn_sensor_bumper_count, R.id.btn_sensor_sonar_status, R.id.btn_sensor_cliff_status,
            R.id.btn_sensor_bumper_status, R.id.rl_get_sensor_detect_info})
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
            case R.id.btn_sensor_sonar_count:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_SONAR_COUNT));
                break;
            case R.id.btn_sensor_cliff_count:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_CLIFF_COUNT));
                break;
            case R.id.btn_sensor_bumper_count:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_BUMPER_COUNT));
                break;
            case R.id.btn_sensor_sonar_status:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_SONAR_STATUS));
                break;
            case R.id.btn_sensor_cliff_status:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_CLIFF_STATUS));
                break;
            case R.id.btn_sensor_bumper_status:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_BUMPER_STATUS));
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

    private void setSensorCount(int tag, int count) {
        showToastTips(getString(R.string.set_sensor_result, count >= 0));

        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String content = count < 0 ? getString(R.string.unknown) : String.valueOf(count);
                switch (tag) {
                    case TAG_SONAR_COUNT:
                        btnSonarCount.setText(getString(R.string.sensor_sonar, content));
                        break;
                    case TAG_CLIFF_COUNT:
                        btnCliffCount.setText(getString(R.string.sensor_cliff, content));
                        break;
                    case TAG_BUMPER_COUNT:
                        btnBumperCount.setText(getString(R.string.sensor_bumper, content));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void setSensorStatus(int tag, List<Integer> list) {
        boolean isSuccess = list != null && !list.isEmpty();
        showToastTips(getString(R.string.set_sensor_result, isSuccess));

        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String content;
                if (isSuccess) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0, size = list.size(); i < size; i++) {
                        if (size > 1) {
                            builder.append(i);
                            builder.append(BaseConstant.SENSOR_STATUS_SPLIT);
                        }

                        builder.append(getSensorReason(list.get(i)));
                        if (i != size - 1) {
                            builder.append(BaseConstant.SPLIT);
                        }
                    }

                    content = builder.toString().trim();
                } else {
                    content = getString(R.string.unknown);
                }

                switch (tag) {
                    case TAG_SONAR_STATUS:
                        btnSonarStatus.setText(getString(R.string.sensor_sonar, content));
                        break;
                    case TAG_CLIFF_STATUS:
                        btnCliffStatus.setText(getString(R.string.sensor_cliff, content));
                        break;
                    case TAG_BUMPER_STATUS:
                        btnBumperStatus.setText(getString(R.string.sensor_bumper, content));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private String getSensorReason(int code) {
        // 状态分2种：0x01表示正常，0x02表示异常，其它值代表无效
        int errorCode = 1;
        if (code == errorCode) {
            return getString(R.string.open);
        }

        int exceptionCode = 2;
        if (code == exceptionCode) {
            return getString(R.string.error);
        }

        return getString(R.string.invalid);
    }

    private class TestRunnable implements Runnable {
        private int mTag;

        private TestRunnable(int tag) {
            mTag = tag;
        }

        @Override
        public void run() {
            switch (mTag) {
                case TAG_SONAR_COUNT:
                    setSensorCount(mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.SONAR));
                    break;
                case TAG_CLIFF_COUNT:
                    setSensorCount(mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.CLIFF));
                    break;
                case TAG_BUMPER_COUNT:
                    setSensorCount(mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.BUMPER));
                    break;
                case TAG_SONAR_STATUS:
                    setSensorStatus(mTag, SlamManager.getInstance().getDefinedSensorStatus(IdType.SONAR));
                    break;
                case TAG_CLIFF_STATUS:
                    setSensorStatus(mTag, SlamManager.getInstance().getDefinedSensorStatus(IdType.CLIFF));
                    break;
                case TAG_BUMPER_STATUS:
                    setSensorStatus(mTag, SlamManager.getInstance().getDefinedSensorStatus(IdType.BUMPER));
                    break;
                case TAG_ROTATE:
                    rotate();
                    break;
                default:
                    break;
            }
        }
    }
}
