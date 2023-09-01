package com.tobot.map.module.main;

import android.content.Context;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.common.MoveData;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.action.OnChargeResultListener;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnChargeListener;
import com.tobot.slam.agent.listener.OnNavigateListener;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2020/5/1
 */
public abstract class AbstractMove implements OnNavigateListener, OnChargeListener {
    protected Context mContext;
    protected MainActivity mActivity;
    private OnChargeResultListener mOnChargeResultListener;

    public AbstractMove(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
        mContext = contextWeakReference.get();
        mActivity = activityWeakReference.get();
    }

    @Override
    public void onNavigateStartTry() {
        Logger.i(BaseConstant.TAG, "onNavigateStartTry()");
    }

    @Override
    public void onNavigateObstacleRemind() {
        Logger.i(BaseConstant.TAG, "onNavigateObstacleRemind()");
        showToast(mContext.getString(R.string.move_remind));
    }

    @Override
    public void onNavigateRelocateBegin() {
        Logger.i(BaseConstant.TAG, "onNavigateRelocateBegin()");
        showRelocateTips();
    }

    @Override
    public void onNavigateRelocateEnd(boolean isRelocateSuccess) {
        Logger.i(BaseConstant.TAG, "onNavigateRelocateEnd() isRelocateSuccess=" + isRelocateSuccess);
        handleRelocateResult(isRelocateSuccess);
    }

    @Override
    public void onNavigateResult(boolean isNavigateSuccess) {
        Logger.i(BaseConstant.TAG, "onNavigateResult() isNavigateSuccess=" + isNavigateSuccess);
        showToast(mContext.getString(R.string.navigate_result, isNavigateSuccess));
        if (!isNavigateSuccess) {
            DataHelper.getInstance().recordImportantInfo(mContext, "navigate fail");
        }
    }

    @Override
    public void onChargeStartTry() {
        Logger.i(BaseConstant.TAG, "onChargeStartTry()");
    }

    @Override
    public void onChargeObstacleRemind() {
        Logger.i(BaseConstant.TAG, "onChargeObstacleRemind()");
        showToast(mContext.getString(R.string.move_remind));
    }

    @Override
    public void onChargeRelocateBegin() {
        Logger.i(BaseConstant.TAG, "onChargeRelocateBegin()");
        showRelocateTips();
    }

    @Override
    public void onChargeRelocateEnd(boolean isRelocateSuccess) {
        Logger.i(BaseConstant.TAG, "onChargeRelocateEnd() isRelocateSuccess=" + isRelocateSuccess);
        handleRelocateResult(isRelocateSuccess);
    }

    @Override
    public void onChargeResult(boolean isChargeSuccess) {
        Logger.i(BaseConstant.TAG, "onChargeResult() isChargeSuccess=" + isChargeSuccess);
        showToast(mContext.getString(R.string.charge_result, isChargeSuccess));
        if (!isChargeSuccess) {
            DataHelper.getInstance().recordImportantInfo(mContext, "charge fail");
        }
    }

    public void moveTo(LocationBean bean) {
        if (bean != null) {
            long tryTime = DataHelper.getInstance().getTryTimeMillis(mContext);
            boolean isObstacleTriggerStop = MoveData.getInstance().getObstacleMode() == MoveData.MEET_OBSTACLE_SUSPEND;
            SlamManager.getInstance().moveTo(bean, MoveData.getInstance().getMoveOption(), tryTime, isObstacleTriggerStop, this);
        }
    }

    public void goCharge(OnChargeResultListener listener) {
        mOnChargeResultListener = listener;
        // 回充要遇障绕行
        long tryTime = DataHelper.getInstance().getTryTimeMillis(mContext);
        SlamManager.getInstance().goHome(DataHelper.getInstance().getChargeDistance(mContext), DataHelper.getInstance().getChargeOffset(mContext), tryTime, this);
    }

    private void showToast(String tips) {
        if (mActivity != null) {
            mActivity.showToast(tips);
        }
    }

    private void showRelocateTips() {
        if (mActivity != null) {
            mActivity.showRelocateTips();
        }
    }

    private void handleRelocateResult(boolean isRelocateSuccess) {
        if (mActivity != null) {
            mActivity.handleRelocateResult(isRelocateSuccess);
        }
    }

    protected void handleMoveFail(boolean isJudgeReason) {
        if (isJudgeReason && isSystemStop()) {
            return;
        }

        if (mActivity != null) {
            mActivity.handleMoveFail();
        }
    }

    protected void callbackChargeResult(boolean isSuccess) {
        if (mOnChargeResultListener != null) {
            try {
                mOnChargeResultListener.onChargeResult(isSuccess);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean isSystemStop() {
        if (SlamManager.getInstance().isSystemEmergencyStop()) {
            showToast(mContext.getString(R.string.emergency_stop_tips));
            return true;
        }

        if (SlamManager.getInstance().isSystemBrakeStop()) {
            showToast(mContext.getString(R.string.break_stop_tips));
            return true;
        }

        if (SlamManager.getInstance().isDirectCharge()) {
            showToast(mContext.getString(R.string.direct_charge_to_navigate_tips));
            return true;
        }

        return false;
    }
}
