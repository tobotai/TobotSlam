package com.tobot.map.module.task;

import android.content.Context;
import android.os.Handler;

import com.tobot.map.R;
import com.tobot.map.base.BaseConstant;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.module.main.AbsPathMonitor;
import com.tobot.map.util.LogUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnNavigateListener;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author houdeming
 * @date 2020/3/17
 */
public class Task extends AbsPathMonitor implements OnNavigateListener {
    private List<LocationBean> mLocationList;
    private int mAllLoopCount, mItemCount, mCurrentLoopCount;
    private boolean isStart;

    public Task(WeakReference<Context> contextWeakReference, WeakReference<Handler> handlerWeakReference) {
        super(contextWeakReference, handlerWeakReference);
    }

    @Override
    public void onNavigateStartTry() {
        LogUtils.i("onNavigateStartTry()");
    }

    @Override
    public void onNavigateRemind() {
        LogUtils.i("onNavigateRemind()");
        showToast(mContext.getString(R.string.navigate_remind));
    }

    @Override
    public void onNavigateSensorTrigger(boolean isEnabled) {
        LogUtils.i("onNavigateSensorTrigger() isEnabled=" + isEnabled);
    }

    @Override
    public void onNavigateError() {
        LogUtils.i("onNavigateError()");
        showToast(mContext.getString(R.string.navigate_error));
        onNavigateResult(false);
    }

    @Override
    public void onNavigateResult(boolean isNavigateSuccess) {
        LogUtils.i("onNavigationResult() isNavigateSuccess=" + isNavigateSuccess);
        showToast(mContext.getString(R.string.navigate_result, isNavigateSuccess));
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

    public void execute(List<LocationBean> data, int loopCount) {
        LogUtils.i("loopCount=" + loopCount);
        mLocationList = data;
        mAllLoopCount = loopCount;
        if (data != null && !data.isEmpty()) {
            isStart = true;
            mCurrentLoopCount = 0;
            mItemCount = 0;
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
                SlamManager.getInstance().moveTo(bean, MoveData.getInstance().getMoveOption(), 0, this);
                return;
            }
            // 无限循环的情况
            if (mAllLoopCount == BaseConstant.LOOP_INFINITE) {
                mItemCount = 0;
                navigate();
                return;
            }
            // 循环指定次数的情况
            mCurrentLoopCount++;
            if (mCurrentLoopCount < mAllLoopCount) {
                mItemCount = 0;
                navigate();
                return;
            }
        }
        stopMonitor();
    }
}
