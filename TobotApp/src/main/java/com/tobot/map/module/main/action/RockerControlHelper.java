package com.tobot.map.module.main.action;

import com.slamtec.slamware.action.MoveDirection;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.slam.SlamManager;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/22
 */
class RockerControlHelper implements RockerView.OnShakeListener {
    private static final int TIME_CONTROL_MOVE_INTERVAL = 80;
    private static final int TIME_CONTROL_ROTATE_INTERVAL = 100;
    private RockerView.Direction mDirection;
    private int mClickCount;
    private boolean isStart;
    private MoveThread mMoveThread;

    RockerControlHelper(WeakReference<RockerView> rockerViewWeakReference) {
        RockerView rockerView = rockerViewWeakReference.get();
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
        rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_4_ROTATE_45, this);
    }

    @Override
    public void onStart() {
        Logger.i(BaseConstant.TAG, "onStart");
    }

    @Override
    public void direction(RockerView.Direction direction) {
        Logger.i(BaseConstant.TAG, "direction=" + direction);
        mDirection = direction;
        if (mMoveThread == null) {
            mClickCount = 0;
            isStart = true;
            mMoveThread = new MoveThread();
            mMoveThread.start();
        }
    }

    @Override
    public void onFinish() {
        Logger.i(BaseConstant.TAG, "onFinish");
        isStart = false;
        if (mMoveThread != null) {
            mMoveThread.interrupt();
            mMoveThread = null;
        }
    }

    private void controlMove(RockerView.Direction direction) {
        // 向前走的时候只考虑直充的情况
        if (direction != RockerView.Direction.DIRECTION_UP) {
            if (SlamManager.getInstance().isBatteryCharging()) {
                isStart = false;
                return;
            }
        }

        switch (direction) {
            case DIRECTION_LEFT:
                SlamManager.getInstance().moveBy(MoveDirection.TURN_LEFT);
                break;
            case DIRECTION_RIGHT:
                SlamManager.getInstance().moveBy(MoveDirection.TURN_RIGHT);
                break;
            case DIRECTION_UP:
                if (SlamManager.getInstance().isDirectCharge()) {
                    isStart = false;
                    return;
                }

                SlamManager.getInstance().moveBy(MoveDirection.FORWARD);
                break;
            case DIRECTION_DOWN:
                SlamManager.getInstance().moveBy(MoveDirection.BACKWARD);
                break;
            default:
                break;
        }
    }

    private class MoveThread extends Thread {

        @Override
        public void run() {
            while (isStart) {
                long delayTime;
                // 左右控制的时候，间隔发送
                if (mDirection == RockerView.Direction.DIRECTION_LEFT || mDirection == RockerView.Direction.DIRECTION_RIGHT) {
                    delayTime = TIME_CONTROL_ROTATE_INTERVAL;
                    if (mClickCount >= 1) {
                        mClickCount = 0;
                    } else {
                        mClickCount++;
                        controlMove(mDirection);
                    }
                } else {
                    delayTime = TIME_CONTROL_MOVE_INTERVAL;
                    controlMove(mDirection);
                }

                if (isStart) {
                    try {
                        Thread.sleep(delayTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

            Logger.i(BaseConstant.TAG, "cancel");
            SlamManager.getInstance().cancelMove();
        }
    }
}
