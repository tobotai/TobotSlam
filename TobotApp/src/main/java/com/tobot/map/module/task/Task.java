package com.tobot.map.module.task;

import android.content.Context;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.AbstractMove;
import com.tobot.map.module.main.MainActivity;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnSystemStopListener;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author houdeming
 * @date 2020/3/17
 */
public class Task extends AbstractMove implements OnSystemStopListener {
    private static final int MOVE_STATUS_IDLE = 0;
    private static final int MOVE_STATUS_NAVIGATE = 1;
    private static final int MOVE_STATUS_CHARGE = 2;
    private List<LocationBean> mLocationList;
    private int mAllLoopCount, mItemCount, mCurrentLoopCount;
    private boolean isAddCharge, isStart, isRelocateSuccess;
    private int mMoveStatus;

    public Task(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
        super(contextWeakReference, activityWeakReference);
    }

    @Override
    public void onNavigateRelocateEnd(boolean isRelocateSuccess) {
        super.onNavigateRelocateEnd(isRelocateSuccess);
        this.isRelocateSuccess = isRelocateSuccess;
    }

    @Override
    public void onNavigateResult(boolean isNavigateSuccess) {
        super.onNavigateResult(isNavigateSuccess);
        handleNavigateResult(isNavigateSuccess);
    }

    @Override
    public void onChargeResult(boolean isChargeSuccess) {
        super.onChargeResult(isChargeSuccess);
        if (isChargeSuccess) {
            loopNavigate();
            return;
        }

        if (isSystemStop()) {
            mMoveStatus = MOVE_STATUS_CHARGE;
            return;
        }

        stop();
        handleMoveFail(false);
    }

    @Override
    public void onSystemStop(boolean isTrigger) {
        Logger.i(BaseConstant.TAG, "system stop isTrigger=" + isTrigger);
        if (!isTrigger && isStart) {
            if (mMoveStatus == MOVE_STATUS_NAVIGATE) {
                mMoveStatus = MOVE_STATUS_IDLE;
                navigate();
                return;
            }

            if (mMoveStatus == MOVE_STATUS_CHARGE) {
                mMoveStatus = MOVE_STATUS_IDLE;
                goCharge();
            }
        }
    }

    public void execute(List<LocationBean> data, boolean isAddCharge, int loopCount) {
        Logger.i(BaseConstant.TAG, "isAddCharge=" + isAddCharge + ",loopCount=" + loopCount);
        mLocationList = data;
        this.isAddCharge = isAddCharge;
        mAllLoopCount = loopCount;
        mMoveStatus = MOVE_STATUS_IDLE;
        if (data != null && !data.isEmpty()) {
            // 默认定位成功
            isRelocateSuccess = true;
            isStart = true;
            mCurrentLoopCount = 0;
            updateTaskCount();
            mItemCount = 0;
            navigate();
            SlamManager.getInstance().startSystemStopMonitor(true, true, 1500, this);
        }
    }

    public void stop() {
        isStart = false;
        SlamManager.getInstance().stopSystemStopMonitor();
    }

    private void navigate() {
        if (isStart) {
            if (mItemCount < mLocationList.size()) {
                moveTo(mLocationList.get(mItemCount));
                return;
            }

            if (isAddCharge) {
                goCharge();
                return;
            }

            loopNavigate();
            return;
        }

        stop();
    }

    private void loopNavigate() {
        mCurrentLoopCount++;
        updateTaskCount();
        // 无限循环的情况或按循环次数
        if (mAllLoopCount == BaseConstant.LOOP_INFINITE || mCurrentLoopCount < mAllLoopCount) {
            mItemCount = 0;
            navigate();
            return;
        }

        stop();
    }

    private void handleNavigateResult(boolean isNavigateSuccess) {
        // 如果紧急停止后，则不再继续
        if (!isNavigateSuccess && isSystemStop()) {
            mMoveStatus = MOVE_STATUS_NAVIGATE;
            return;
        }

        // 如果不是因为定位问题则继续导航去下一个点
        if (isNavigateSuccess || isRelocateSuccess) {
            mItemCount++;
            navigate();
            return;
        }

        stop();
        handleMoveFail(false);
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
