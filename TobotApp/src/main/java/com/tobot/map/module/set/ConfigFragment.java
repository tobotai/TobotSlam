package com.tobot.map.module.set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.set.firmware.FirmwareUpgradeActivity;
import com.tobot.map.module.set.firmware.SetSensorDataReportedActivity;
import com.tobot.map.util.NumberUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;
import com.tobot.slam.agent.listener.OnResultListener;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2019/10/21
 */
public class ConfigFragment extends BaseFragment implements BaseBar.OnSeekBarChangeListener {
    private static final int MSG_REQUEST_NAVIGATE_SPEED = 1;
    private static final int MSG_REQUEST_ROTATE_SPEED = 2;
    private static final int MSG_SET_SPEED_RESULT = 3;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_speed_low)
    RadioButton rbSpeedLow;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_speed_medium)
    RadioButton rbSpeedMedium;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_speed_high)
    RadioButton rbSpeedHigh;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_current_rotate_speed_tips)
    TextView tvCurrentRotateSpeed;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.sb_rotate_speed)
    StripSeekBar sbRotateSpeed;
    private MainHandler mMainHandler;
    private float mSpeed;

    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_config;
    }

    @Override
    protected void init() {
        sbRotateSpeed.setOnSeekBarChangeListener(this);
        mMainHandler = new MainHandler(new WeakReference<>(this));
        requestNavigateSpeed();
        requestRotateSpeed();
    }

    @Override
    public void onPause() {
        super.onPause();
        closeLoadTipsDialog();
        closeConfirmDialog();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == BaseConstant.CODE_EXIT) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.setResult(BaseConstant.CODE_EXIT);
                activity.finish();
            }
        }
    }

    @Override
    public void onSeekBarStart(View view) {
    }

    @Override
    public void onProgressChange(View view, float progress) {
        setProgress(view, progress);
    }

    @Override
    public void onSeekBarStop(View view, float progress) {
        setProgress(view, progress);
        if (view.getId() == R.id.sb_rotate_speed) {
            setRotateSpeed(mSpeed);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.rl_navigate_option, R.id.rl_set_sensor_status, R.id.rl_firmware_upgrade, R.id.rl_navigate_parameter, R.id.rb_speed_low, R.id.rb_speed_medium, R.id.rb_speed_high})
    public void onClickView(View v) {
        switch (v.getId()) {
            case R.id.rb_speed_low:
                setNavigateSpeed(SlamCode.SPEED_LOW);
                break;
            case R.id.rb_speed_medium:
                setNavigateSpeed(SlamCode.SPEED_MEDIUM);
                break;
            case R.id.rb_speed_high:
                setNavigateSpeed(SlamCode.SPEED_HIGH);
                break;
            case R.id.rl_navigate_option:
                startActivity(new Intent(getActivity(), NavigateOptionActivity.class));
                break;
            case R.id.rl_set_sensor_status:
                startActivity(new Intent(getActivity(), SetSensorDataReportedActivity.class));
                break;
            case R.id.rl_firmware_upgrade:
                startActivityForResult(new Intent(getActivity(), FirmwareUpgradeActivity.class), 1);
                break;
            case R.id.rl_navigate_parameter:
                startActivity(new Intent(getActivity(), RunParameterActivity.class));
                break;
            default:
                break;
        }
    }

    private static class MainHandler extends Handler {
        private final ConfigFragment mFragment;

        private MainHandler(WeakReference<ConfigFragment> reference) {
            super(Looper.getMainLooper());
            mFragment = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mFragment == null) {
                return;
            }

            switch (msg.what) {
                case MSG_REQUEST_NAVIGATE_SPEED:
                    String navigateSpeed = (String) msg.obj;
                    if (NumberUtils.isDoubleOrFloat(navigateSpeed)) {
                        mFragment.updateNavigateSpeedView(Float.parseFloat(navigateSpeed));
                    }
                    break;
                case MSG_REQUEST_ROTATE_SPEED:
                    String rotateSpeed = (String) msg.obj;
                    if (NumberUtils.isDoubleOrFloat(rotateSpeed)) {
                        mFragment.updateRotateSpeedView(Float.parseFloat(rotateSpeed));
                    }
                    break;
                case MSG_SET_SPEED_RESULT:
                    mFragment.handleSpeedSetResult((boolean) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private void setProgress(View view, float progress) {
        if (view.getId() == R.id.sb_rotate_speed) {
            float value = progress * BaseConstant.MAX_ROTATE_SPEED;
            // 限制最小速度
            if (value < BaseConstant.MIN_ROTATE_SPEED) {
                value = BaseConstant.MIN_ROTATE_SPEED;
                sbRotateSpeed.setProgress(value / BaseConstant.MAX_ROTATE_SPEED);
            }

            value = NumberUtils.getOneDigitFloat(value);
            mSpeed = value;
            tvCurrentRotateSpeed.setText(getString(R.string.tv_current_rotate_speed_tips, value));
        }
    }

    private void updateNavigateSpeedView(float value) {
        if (value <= SlamCode.SPEED_LOW) {
            rbSpeedLow.setChecked(true);
            return;
        }

        if (value <= SlamCode.SPEED_MEDIUM) {
            rbSpeedMedium.setChecked(true);
            return;
        }

        rbSpeedHigh.setChecked(true);
    }

    private void updateRotateSpeedView(float value) {
        sbRotateSpeed.setProgress(value / BaseConstant.MAX_ROTATE_SPEED);
        tvCurrentRotateSpeed.setText(getString(R.string.tv_current_rotate_speed_tips, value));
    }

    private void requestNavigateSpeed() {
        if (SlamManager.getInstance().isConnected()) {
            SlamManager.getInstance().requestSpeedAsync(new OnResultListener<String>() {
                @Override
                public void onResult(String speed) {
                    Logger.i(BaseConstant.TAG, "navigate speed=" + speed);
                    if (mMainHandler != null) {
                        mMainHandler.obtainMessage(MSG_REQUEST_NAVIGATE_SPEED, speed).sendToTarget();
                    }
                }
            });
            return;
        }

        updateRotateSpeedView(0);
    }

    private void requestRotateSpeed() {
        if (SlamManager.getInstance().isConnected()) {
            SlamManager.getInstance().requestRotateSpeedAsync(new OnResultListener<String>() {
                @Override
                public void onResult(String speed) {
                    Logger.i(BaseConstant.TAG, "rotate speed=" + speed);
                    if (mMainHandler != null) {
                        mMainHandler.obtainMessage(MSG_REQUEST_ROTATE_SPEED, speed).sendToTarget();
                    }
                }
            });
        }
    }

    private void setNavigateSpeed(float value) {
        if (SlamManager.getInstance().isConnected()) {
            SlamManager.getInstance().setSpeedAsync(String.valueOf(value), new OnResultListener<Boolean>() {
                @Override
                public void onResult(Boolean data) {
                    if (mMainHandler != null) {
                        mMainHandler.obtainMessage(MSG_SET_SPEED_RESULT, data).sendToTarget();
                    }
                }
            });
            return;
        }

        showToastTips(getString(R.string.slam_not_connect_tips));
    }

    private void setRotateSpeed(float value) {
        if (SlamManager.getInstance().isConnected()) {
            SlamManager.getInstance().setRotateSpeedAsync(String.valueOf(value), new OnResultListener<Boolean>() {
                @Override
                public void onResult(Boolean data) {
                    if (mMainHandler != null) {
                        mMainHandler.obtainMessage(MSG_SET_SPEED_RESULT, data).sendToTarget();
                    }
                }
            });
            return;
        }

        showToastTips(getString(R.string.slam_not_connect_tips));
    }

    private void handleSpeedSetResult(boolean isSuccess) {
        showToastTips(isSuccess ? getString(R.string.set_success) : getString(R.string.set_fail));
    }
}
