package com.tobot.map.module.task;

import android.content.Context;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.AbstractPathMonitor;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.module.main.MainActivity;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnChargeListener;
import com.tobot.slam.agent.listener.OnNavigateListener;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author houdeming
 * @date 2020/3/17
 */
public class Task extends AbstractPathMonitor implements OnNavigateListener, OnChargeListener {
    private List<LocationBean> mLocationList;
    private int mAllLoopCount, mItemCount, mCurrentLoopCount;
    private boolean isAddCharge, isStart;

    public Task(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
        super(contextWeakReference, activityWeakReference);
    }

    @Override
    public void onNavigateStartTry() {
        Logger.i(BaseConstant.TAG, "onNavigateStartTry()");
    }

    @Override
    public void onNavigateRemind() {
        Logger.i(BaseConstant.TAG, "onNavigateRemind()");
        showToast(mContext.getString(R.string.navigate_remind));
    }

    @Override
    public void onNavigateSensorTrigger(boolean isEnabled) {
        Logger.i(BaseConstant.TAG, "onNavigateSensorTrigger() isEnabled=" + isEnabled);
    }

    @Override
    public void onNavigateError() {
        Logger.i(BaseConstant.TAG, "onNavigateError()");
        showToast(mContext.getString(R.string.navigate_error));
        onNavigateResult(false);
    }

    @Override
    public void onNavigateResult(boolean isNavigateSuccess) {
        Logger.i(BaseConstant.TAG, "onNavigationResult() isNavigateSuccess=" + isNavigateSuccess);
        showToast(mContext.getString(R.string.navigate_result, isNavigateSuccess));
        if (mActivity != null) {
            mActivity.handleMoveResult(isNavigateSuccess);
        }

        mItemCount++;
        navigate();
    }

    @Override
    public void onObstacleTrigger() {
        SlamManager.getInstance().cancelAction();
    }

    @Override
    public void onObstacleDisappear() {
        navigate();
    }

    @Override
    public void onChargeSensorTrigger(boolean isEnabled) {
        Logger.i(BaseConstant.TAG, "onChargeSensorTrigger() isEnabled=" + isEnabled);
    }

    @Override
    public void onChargeError() {
        Logger.i(BaseConstant.TAG, "onChargeError()");
    }

    @Override
    public void onCharging() {
        Logger.i(BaseConstant.TAG, "onCharging()");
        onChargeResult(true);
    }

    @Override
    public void onChargeResult(boolean isChargeSuccess) {
        Logger.i(BaseConstant.TAG, "onChargeResult() isChargeSuccess=" + isChargeSuccess);
        showToast(mContext.getString(R.string.charge_result, isChargeSuccess));
        if (isChargeSuccess) {
            updateTaskCount();
            if (mAllLoopCount == BaseConstant.LOOP_INFINITE || mCurrentLoopCount < mAllLoopCount) {
                mItemCount = 0;
                navigate();
            }
            return;
        }

        if (mActivity != null) {
            mActivity.handleMoveResult(false);
        }
    }

    public void execute(List<LocationBean> data, boolean isAddCharge, int loopCount) {
        Logger.i(BaseConstant.TAG, "isAddCharge=" + isAddCharge + ",loopCount=" + loopCount);
        mLocationList = data;
        this.isAddCharge = isAddCharge;
        mAllLoopCount = loopCount;
        if (data != null && !data.isEmpty()) {
            isStart = true;
            mCurrentLoopCount = 0;
            mItemCount = 0;
            updateTaskCount();
            startMonitor();
            navigate();
        }
    }

    public void stop() {
        isStart = false;
        stopMonitor();
    }

    private void navigate() {
        if (isStart) {
            if (mItemCount < mLocationList.size()) {
                LocationBean bean = mLocationList.get(mItemCount);
                long tryTime = 0;
                if (MoveData.getInstance().getObstacleMode() == MoveData.MEET_OBSTACLE_AVOID) {
                    tryTime = DataHelper.getInstance().getTryTimeMillis(mContext);
                }
                SlamManager.getInstance().moveTo(bean, MoveData.getInstance().getMoveOption(), tryTime, this);
                return;
            }

            mCurrentLoopCount++;
            // 无限循环的情况
            if (mAllLoopCount == BaseConstant.LOOP_INFINITE) {
                continueNavigate();
                return;
            }

            // 循环指定次数的情况
            if (mCurrentLoopCount < mAllLoopCount) {
                continueNavigate();
                return;
            }

            if (isAddCharge) {
                SlamManager.getInstance().goHome(this);
                return;
            }

            updateTaskCount();
        }

        stopMonitor();
    }

    private void continueNavigate() {
        if (isAddCharge) {
            SlamManager.getInstance().goHome(this);
            return;
        }

        updateTaskCount();
        mItemCount = 0;
        navigate();
    }

    private void updateTaskCount() {
        if (mActivity != null) {
            String content;
            if (mAllLoopCount == BaseConstant.LOOP_INFINITE) {
                content = mContext.getString(R.string.task_time_infinite, mCurrentLoopCount);
            } else {
                content = mContext.getString(R.string.task_time_limited, mCurrentLoopCount, mAllLoopCount);
            }
            mActivity.setTaskCount(content);
        }
    }
}
