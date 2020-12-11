package com.tobot.map.module.set;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.module.common.TipsDialog;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.module.set.wifi.WifiConfigDialog;
import com.tobot.map.util.LogUtils;
import com.tobot.map.util.NumberUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnResultListener;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2019/10/21
 */
public class ConfigFragment extends BaseFragment implements BaseBar.OnSeekBarChangeListener, WifiConfigDialog.OnWifiListener {
    private static final int MSG_REQUEST_SPEED = 1;
    private static final int MSG_SET_SPEED = 2;
    private static final int MSG_REQUEST_ROTATE_SPEED = 3;
    private static final int MSG_MODE_CONFIG = 4;
    @BindView(R.id.tv_current_speed_tips)
    TextView tvCurrentSpeed;
    @BindView(R.id.sb_speed)
    StripSeekBar sbSpeed;
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
    private WifiConfigDialog mWifiConfigDialog;
    private TipsDialog mTipsDialog;
    private boolean isConfigApMode;
    private float mMaxSpeed = 0.7f;
    private float mMaxRotateSpeed = 2f;
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
        sbSpeed.setOnSeekBarChangeListener(this);
        sbRotateSpeed.setOnSeekBarChangeListener(this);
        sbTryTime.setOnSeekBarChangeListener(this);
        mMainHandler = new MainHandler(new WeakReference<>(this));
        setNavigateMode(MoveData.getInstance().getNavigateMode());
        setMotionMode(MoveData.getInstance().getMotionMode());
        setObstacleMode(MoveData.getInstance().getObstacleMode());
        requestSpeed();
        requestRotateSpeed();
    }

    @Override
    public void onPause() {
        super.onPause();
        closeLoadTipsDialog();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
        if (isWifiConfigDialogShow()) {
            mWifiConfigDialog.getDialog().dismiss();
            mWifiConfigDialog = null;
        }
        closeConfirmDialog();
        if (isTipsDialogShow()) {
            mTipsDialog.getDialog().dismiss();
            mTipsDialog = null;
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
            case R.id.sb_speed:
                setSpeed(mSpeed);
                break;
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

    @Override
    public void onWifi(String wifiName, String wifiPwd) {
        LogUtils.i("wifiName=" + wifiName + ",wifiPwd=" + wifiPwd);
        showLoadTipsDialog(getString(R.string.set_ing_tips));
        isConfigApMode = false;
        SlamManager.getInstance().configWifiInThread(wifiName, wifiPwd, new OnResultListener<Boolean>() {
            @Override
            public void onResult(Boolean data) {
                if (mMainHandler != null) {
                    mMainHandler.obtainMessage(MSG_MODE_CONFIG, data).sendToTarget();
                }
            }
        });
    }

    @Override
    public void onConfirm() {
        super.onConfirm();
        showLoadTipsDialog(getString(R.string.set_ing_tips));
        isConfigApMode = true;
        SlamManager.getInstance().configApInThread(new OnResultListener<Boolean>() {
            @Override
            public void onResult(Boolean data) {
                if (mMainHandler != null) {
                    mMainHandler.obtainMessage(MSG_MODE_CONFIG, data).sendToTarget();
                }
            }
        });
    }

    @OnClick({R.id.rb_navigate_free, R.id.rb_navigate_track, R.id.rb_navigate_track_first, R.id.rb_motion_exact, R.id.rb_motion_ordinary,
            R.id.rb_obstacle_avoid, R.id.rb_obstacle_suspend, R.id.rl_config_ap, R.id.rl_config_wifi})
    public void onClickView(View v) {
        switch (v.getId()) {
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
            case R.id.rl_config_ap:
                configAp();
                break;
            case R.id.rl_config_wifi:
                configWifi();
                break;
            default:
                break;
        }
    }

    private static class MainHandler extends Handler {
        private ConfigFragment mFragment;

        private MainHandler(WeakReference<ConfigFragment> reference) {
            super();
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
                case MSG_MODE_CONFIG:
                    if (mFragment != null) {
                        mFragment.handleModeConfigResult((boolean) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void setSpeed(View view, float progress) {
        switch (view.getId()) {
            case R.id.sb_speed:
                mSpeed = progress * mMaxSpeed;
                tvCurrentSpeed.setText(getString(R.string.tv_current_speed_tips, mSpeed));
                break;
            case R.id.sb_rotate_speed:
                mSpeed = progress * mMaxRotateSpeed;
                tvCurrentRotateSpeed.setText(getString(R.string.tv_current_rotate_speed_tips, mSpeed));
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
        sbSpeed.setProgress(value / mMaxSpeed);
        tvCurrentSpeed.setText(getString(R.string.tv_current_speed_tips, value));
    }

    private void updateRotateSpeedView(float value) {
        sbRotateSpeed.setProgress(value / mMaxRotateSpeed);
        tvCurrentRotateSpeed.setText(getString(R.string.tv_current_rotate_speed_tips, value));
    }

    private void requestSpeed() {
        if (SlamManager.getInstance().isConnected()) {
            SlamManager.getInstance().requestSpeedInThread(new OnResultListener<String>() {
                @Override
                public void onResult(String speed) {
                    LogUtils.i("speed=" + speed);
                    if (mMainHandler != null) {
                        mMainHandler.obtainMessage(MSG_REQUEST_SPEED, speed).sendToTarget();
                    }
                }
            });
            return;
        }
        // 显示默认速度
        updateSpeedView(MoveData.DEFAULT_SPEED);
    }

    private void setSpeed(float value) {
        if (SlamManager.getInstance().isConnected()) {
            SlamManager.getInstance().setSpeedInThread(String.valueOf(value), new OnResultListener<Boolean>() {
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
            SlamManager.getInstance().requestRotateSpeedInThread(new OnResultListener<String>() {
                @Override
                public void onResult(String speed) {
                    LogUtils.i("rotate speed=" + speed);
                    if (mMainHandler != null) {
                        mMainHandler.obtainMessage(MSG_REQUEST_ROTATE_SPEED, speed).sendToTarget();
                    }
                }
            });
            return;
        }
        // 显示默认速度
        updateRotateSpeedView(MoveData.DEFAULT_ROTATE_SPEED);
    }

    private void setRotateSpeed(float value) {
        if (SlamManager.getInstance().isConnected()) {
            SlamManager.getInstance().setRotateSpeedInThread(String.valueOf(value), new OnResultListener<Boolean>() {
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

    private void configAp() {
        if (SlamManager.getInstance().isConnected()) {
            showConfirmDialog(getString(R.string.set_ap_confirm_tips));
            return;
        }
        showToastTips(getString(R.string.slam_not_connect_tips));
    }

    private void configWifi() {
        if (SlamManager.getInstance().isConnected()) {
            if (!isWifiConfigDialogShow()) {
                mWifiConfigDialog = WifiConfigDialog.newInstance();
                mWifiConfigDialog.setOnWifiListener(this);
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    mWifiConfigDialog.show(fragmentManager, "STA_DIALOG");
                }
            }
            return;
        }
        showToastTips(getString(R.string.slam_not_connect_tips));
    }

    private void handleModeConfigResult(boolean isSuccess) {
        closeLoadTipsDialog();
        showToastTips(isSuccess ? getString(R.string.set_success_tips) : getString(R.string.set_fail_tips));
        if (!isTipsDialogShow()) {
            String tips = isConfigApMode ? getString(R.string.mode_config_success_tips, getString(R.string.ap_config_success)) : getString(R.string.mode_config_success_tips, getString(R.string.sta_config_success));
            mTipsDialog = TipsDialog.newInstance(tips);
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                mTipsDialog.show(fragmentManager, "TIPS_DIALOG");
            }
        }
    }

    private boolean isWifiConfigDialogShow() {
        return mWifiConfigDialog != null && mWifiConfigDialog.getDialog() != null && mWifiConfigDialog.getDialog().isShowing();
    }

    private boolean isTipsDialogShow() {
        return mTipsDialog != null && mTipsDialog.getDialog() != null && mTipsDialog.getDialog().isShowing();
    }
}
