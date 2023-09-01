package com.tobot.map.module.set;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.SystemUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2022/06/24
 */
public class NavigateOptionActivity extends BaseBackActivity implements BaseBar.OnSeekBarChangeListener, Runnable {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_navigate_free)
    RadioButton rbNavigateFree;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_navigate_track)
    RadioButton rbNavigateTrack;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_navigate_track_first)
    RadioButton rbNavigateTrackFirst;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_motion_ordinary)
    RadioButton rbMotionOrdinary;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_motion_exact)
    RadioButton rbMotionExact;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_obstacle_avoid)
    RadioButton rbObstacleAvoid;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_obstacle_suspend)
    RadioButton rbObstacleSuspend;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_try_time)
    TextView tvTryTime;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.sb_try_time)
    StripSeekBar sbTryTime;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_try_time_tips)
    TextView tvTryTimeTips;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_map_location_switch)
    TextView tvLocationSwitch;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_speed_multiplier)
    TextView tvSpeedMultiplier;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_touch_count)
    TextView tvTouchCount;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_touch_count)
    EditText etTouchCount;
    private static final int TYPE_MAP_LOCATION = 0;
    private static final int TYPE_SPEED_FAST = 1;
    private boolean isObstacleAvoid;
    private int mTime;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_navigate_option;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_navigate_option);
        sbTryTime.setOnSeekBarChangeListener(this);
        setNavigateMode(MoveData.getInstance().getNavigateMode());
        setMotionMode(MoveData.getInstance().getMotionMode());
        setObstacleMode(MoveData.getInstance().getObstacleMode(), true);
        ThreadPoolManager.getInstance().execute(this);
        tvSpeedMultiplier.setSelected(BaseConstant.isSpeedFast);
        tvTouchCount.setText(getString(R.string.tv_touch_count, DataHelper.getInstance().getTouchCount(this)));
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
        if (view.getId() == R.id.sb_try_time) {
            DataHelper.getInstance().setTryTime(this, mTime);
        }
    }

    @Override
    public void run() {
        boolean isLocation = SlamManager.getInstance().getMapLocalization();
        Logger.i(BaseConstant.TAG, "map isLocation=" + isLocation);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLocationSwitch.setSelected(isLocation);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.rb_navigate_free, R.id.rb_navigate_track, R.id.rb_navigate_track_first, R.id.rb_motion_exact, R.id.rb_motion_ordinary,
            R.id.rb_obstacle_avoid, R.id.rb_obstacle_suspend, R.id.tv_map_location_switch, R.id.tv_speed_multiplier, R.id.btn_set_touch_count})
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
                setObstacleMode(MoveData.MEET_OBSTACLE_AVOID, false);
                break;
            case R.id.rb_obstacle_suspend:
                setObstacleMode(MoveData.MEET_OBSTACLE_SUSPEND, false);
                break;
            case R.id.tv_map_location_switch:
                if (!SlamManager.getInstance().isConnected()) {
                    showToastTips(getString(R.string.slam_not_connect_tips));
                    return;
                }

                tvLocationSwitch.setSelected(!tvLocationSwitch.isSelected());
                ThreadPoolManager.getInstance().execute(new SetRunnable(TYPE_MAP_LOCATION, tvLocationSwitch.isSelected()));
                break;
            case R.id.tv_speed_multiplier:
                if (!SlamManager.getInstance().isConnected()) {
                    showToastTips(getString(R.string.slam_not_connect_tips));
                    return;
                }

                tvSpeedMultiplier.setSelected(!tvSpeedMultiplier.isSelected());
                ThreadPoolManager.getInstance().execute(new SetRunnable(TYPE_SPEED_FAST, tvSpeedMultiplier.isSelected()));
                break;
            case R.id.btn_set_touch_count:
                setTouchCount();
                break;
            default:
                break;
        }
    }

    private void setNavigateMode(int mode) {
        switch (mode) {
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

    private void setMotionMode(int mode) {
        if (mode == MoveData.MOTION_TO_POINT_EXACT) {
            rbMotionExact.setChecked(true);
            return;
        }

        rbMotionOrdinary.setChecked(true);
    }

    private void setObstacleMode(int mode, boolean isInit) {
        isObstacleAvoid = mode == MoveData.MEET_OBSTACLE_AVOID;
        if (isInit) {
            if (isObstacleAvoid) {
                rbObstacleAvoid.setChecked(true);
            } else {
                rbObstacleSuspend.setChecked(true);
            }
        } else {
            MoveData.getInstance().setObstacleMode(mode);
        }

        mTime = DataHelper.getInstance().getTryTime(this);
        tvTryTime.setText(getString(isObstacleAvoid ? R.string.tv_try_time : R.string.tv_wait_time, mTime));
        tvTryTimeTips.setText(getString(isObstacleAvoid ? R.string.tv_try_time_describe : R.string.tv_wait_time_describe));
        float progress = mTime / BaseConstant.TRY_TIME_MAX;
        sbTryTime.setProgress(progress);
    }

    private void setProgress(View view, float progress) {
        if (view.getId() == R.id.sb_try_time) {
            mTime = (int) (progress * BaseConstant.TRY_TIME_MAX);
            tvTryTime.setText(getString(isObstacleAvoid ? R.string.tv_try_time : R.string.tv_wait_time, mTime));
        }
    }

    private void setTouchCount() {
        String countStr = etTouchCount.getText().toString().trim();
        if (TextUtils.isEmpty(countStr)) {
            showToastTips(getString(R.string.touch_count_empty_tips));
            return;
        }

        SystemUtils.hideKeyboard(this);
        if (TextUtils.isDigitsOnly(countStr)) {
            int count = Integer.parseInt(countStr);
            DataHelper.getInstance().setTouchCount(this, count);
            tvTouchCount.setText(getString(R.string.tv_touch_count, count));
            showToastTips(getString(R.string.set_success_and_reenter));
            etTouchCount.setText("");
        }
    }

    private class SetRunnable implements Runnable {
        private final int mType;
        private final boolean isFlag;

        public SetRunnable(int type, boolean isFlag) {
            this.mType = type;
            this.isFlag = isFlag;
        }

        @Override
        public void run() {
            if (mType == TYPE_MAP_LOCATION) {
                handleMapLocation(isFlag);
                return;
            }

            handleSpeed(isFlag);
        }

        private void handleMapLocation(boolean isLocation) {
            boolean isSuccess = SlamManager.getInstance().setMapLocalization(isLocation);
            Logger.i(BaseConstant.TAG, "set mapLocation isSuccess=" + isSuccess);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isSuccess) {
                        tvLocationSwitch.setSelected(!isLocation);
                    }
                    showToastTips(getString(isSuccess ? R.string.set_success : R.string.set_fail));
                }
            });
        }

        private void handleSpeed(boolean isFast) {
            boolean isSuccess = SlamManager.getInstance().setDefinedMoveFast(isFast);
            Logger.i(BaseConstant.TAG, "set speed fast isSuccess=" + isSuccess);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isSuccess) {
                        tvSpeedMultiplier.setSelected(!isFast);
                    }
                    BaseConstant.isSpeedFast = tvSpeedMultiplier.isSelected();
                    showToastTips(isSuccess ? getString(R.string.set_success) : getString(R.string.set_fail));
                }
            });
        }
    }
}
