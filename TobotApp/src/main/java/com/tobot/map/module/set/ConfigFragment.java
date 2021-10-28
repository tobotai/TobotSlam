package com.tobot.map.module.set;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.DataHelper;
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
    private static final int MSG_REQUEST_SPEED = 1;
    private static final int MSG_SET_SPEED = 2;
    private static final int MSG_REQUEST_ROTATE_SPEED = 3;
    @BindView(R.id.rb_speed_low)
    RadioButton rbSpeedLow;
    @BindView(R.id.rb_speed_medium)
    RadioButton rbSpeedMedium;
    @BindView(R.id.rb_speed_high)
    RadioButton rbSpeedHigh;
    @BindView(R.id.tv_current_rotate_speed_tips)
    TextView tvCurrentRotateSpeed;
    @BindView(R.id.sb_rotate_speed)
    StripSeekBar sbRotateSpeed;
    @BindView(R.id.rb_navigate_free)
    RadioButton rbNavigateFree;
    @BindView(R.id.rb_navigate_track)
    RadioButton rbNavigateTrack;
    @BindView(R.id.rb_navigate_track_first)
    RadioButton rbNavigateTrackFirst;
    @BindView(R.id.rb_motion_ordinary)
    RadioButton rbMotionOrdinary;
    @BindView(R.id.rb_motion_exact)
    RadioButton rbMotionExact;
    @BindView(R.id.rb_obstacle_avoid)
    RadioButton rbObstacleAvoid;
    @BindView(R.id.rb_obstacle_suspend)
    RadioButton rbObstacleSuspend;
    @BindView(R.id.ll_try_time)
    LinearLayout llTryTime;
    @BindView(R.id.tv_try_time)
    TextView tvTryTime;
    @BindView(R.id.sb_try_time)
    StripSeekBar sbTryTime;
    private MainHandler mMainHandler;
    private float mSpeed;
    private int mTime;

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
        sbTryTime.setOnSeekBarChangeListener(this);
        mMainHandler = new MainHandler(new WeakReference<>(this));
        setNavigateMode(MoveData.getInstance().getNavigateMode());
        setMotionMode(MoveData.getInstance().getMotionMode());
        setObstacleMode(MoveData.getInstance().getObstacleMode());
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
        setSpeed(view, progress);
    }

    @Override
    public void onSeekBarStop(View view, float progress) {
        setSpeed(view, progress);
        switch (view.getId()) {
            case R.id.sb_rotate_speed:
                setRotateSpeed(mSpeed);
                break;
            case R.id.sb_try_time:
                DataHelper.getInstance().setTryTime(getActivity(), mTime);
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.rb_navigate_free, R.id.rb_navigate_track, R.id.rb_navigate_track_first, R.id.rb_motion_exact, R.id.rb_motion_ordinary,
            R.id.rb_obstacle_avoid, R.id.rb_obstacle_suspend, R.id.rl_set_sensor_status, R.id.rl_firmware_upgrade, R.id.rl_navigate_parameter,
            R.id.rb_speed_low, R.id.rb_speed_medium, R.id.rb_speed_high})
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
            case R.id.rb_navigate_free:
                MoveData.getInstance().setNavigateMode(MoveData.NAVIGATE_FREE);
                break;
            case R.id.rb_navigate_track:
                MoveData.getInstance().setNavigateMode(MoveData.NAVIGATE_TRACK);
                break;
            case R.id.rb_navigate_track_first:
                MoveData.getInstance().setNavigateMode(MoveData.NAVIGATE_TRACK_FIRST);
                break;
            case R.id.rb_motion_exact:
                MoveData.getInstance().setMotionMode(MoveData.MOTION_TO_POINT_EXACT);
                break;
            case R.id.rb_motion_ordinary:
                MoveData.getInstance().setMotionMode(MoveData.MOTION_TO_POINT_ORDINARY);
                break;
            case R.id.rb_obstacle_avoid:
                MoveData.getInstance().setObstacleMode(MoveData.MEET_OBSTACLE_AVOID);
                setTryTime();
                break;
            case R.id.rb_obstacle_suspend:
                MoveData.getInstance().setObstacleMode(MoveData.MEET_OBSTACLE_SUSPEND);
                llTryTime.setVisibility(View.GONE);
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
        private ConfigFragment mFragment;

        private MainHandler(WeakReference<ConfigFragment> reference) {
            super(Looper.getMainLooper());
            mFragment = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REQUEST_SPEED:
                    if (mFragment != null) {
                        String speed = (String) msg.obj;
                        if (NumberUtils.isDoubleOrFloat(speed)) {
                            mFragment.updateSpeedView(Float.parseFloat(speed));
                        }
                    }
                    break;
                case MSG_SET_SPEED:
                    if (mFragment != null) {
                        mFragment.handleSpeedSetResult((boolean) msg.obj);
                    }
                    break;
                case MSG_REQUEST_ROTATE_SPEED:
                    if (mFragment != null) {
                        String speed = (String) msg.obj;
                        if (NumberUtils.isDoubleOrFloat(speed)) {
                            mFragment.updateRotateSpeedView(Float.parseFloat(speed));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void setSpeed(View view, float progress) {
        float value;
        switch (view.getId()) {
            case R.id.sb_rotate_speed:
                value = progress * BaseConstant.MAX_ROTATE_SPEED;
                // 限制最小速度
                if (value < BaseConstant.MIN_ROTATE_SPEED) {
                    value = BaseConstant.MIN_ROTATE_SPEED;
                    sbRotateSpeed.setProgress(value / BaseConstant.MAX_ROTATE_SPEED);
                }

                mSpeed = value;
                tvCurrentRotateSpeed.setText(getString(R.string.tv_current_rotate_speed_tips, value));
                break;
            case R.id.sb_try_time:
                mTime = (int) (progress * BaseConstant.TRY_TIME_MAX);
                tvTryTime.setText(getString(R.string.tv_try_time_tips, mTime));
                break;
            default:
                break;
        }
    }

    private void setNavigateMode(int navigateMode) {
        switch (navigateMode) {
            case MoveData.NAVIGATE_FREE:
                rbNavigateFree.setChecked(true);
                break;
            case MoveData.NAVIGATE_TRACK:
                rbNavigateTrack.setChecked(true);
                break;
            case MoveData.NAVIGATE_TRACK_FIRST:
                rbNavigateTrackFirst.setChecked(true);
                break;
            default:
                break;
        }
    }

    private void setMotionMode(int motionMode) {
        if (motionMode == MoveData.MOTION_TO_POINT_EXACT) {
            rbMotionExact.setChecked(true);
            return;
        }

        rbMotionOrdinary.setChecked(true);
    }

    private void setObstacleMode(int obstacleMode) {
        if (obstacleMode == MoveData.MEET_OBSTACLE_AVOID) {
            rbObstacleAvoid.setChecked(true);
            setTryTime();
            return;
        }

        rbObstacleSuspend.setChecked(true);
        llTryTime.setVisibility(View.GONE);
    }

    private void setTryTime() {
        llTryTime.setVisibility(View.VISIBLE);
        mTime = DataHelper.getInstance().getTryTime(getActivity());
        tvTryTime.setText(getString(R.string.tv_try_time_tips, mTime));
        float progress = mTime / BaseConstant.TRY_TIME_MAX;
        sbTryTime.setProgress(progress);
    }

    private void updateSpeedView(float value) {
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
                    Logger.i(BaseConstant.TAG, "speed=" + speed);
                    if (mMainHandler != null) {
                        mMainHandler.obtainMessage(MSG_REQUEST_SPEED, speed).sendToTarget();
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
                        mMainHandler.obtainMessage(MSG_SET_SPEED, data).sendToTarget();
                    }
                }
            });
            return;
        }

        showToastTips(getString(R.string.slam_not_connect_tips));
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

    private void setRotateSpeed(float value) {
        if (SlamManager.getInstance().isConnected()) {
            SlamManager.getInstance().setRotateSpeedAsync(String.valueOf(value), new OnResultListener<Boolean>() {
                @Override
                public void onResult(Boolean data) {
                    if (mMainHandler != null) {
                        mMainHandler.obtainMessage(MSG_SET_SPEED, data).sendToTarget();
                    }
                }
            });
            return;
        }

        showToastTips(getString(R.string.slam_not_connect_tips));
    }

    private void handleSpeedSetResult(boolean isSuccess) {
        showToastTips(isSuccess ? getString(R.string.set_success_tips) : getString(R.string.set_fail_tips));
    }
}
