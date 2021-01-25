package com.tobot.map.module.main.action;

import com.slamtec.slamware.action.MoveDirection;
import com.tobot.map.util.LogUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/22
 */
class RockerControlHelper implements RockerView.OnShakeListener {
    private static final int TIME_CONTROL_MOVE_INTERVAL = 80;
    private static final int TIME_CONTROL_ROTATE_INTERVAL = 100;
    private boolean isStart;
    private RockerView.Direction mDirection;
    private int mClickCount;
    private MoveRunnable mMoveRunnable;

    RockerControlHelper(WeakReference<RockerView> rockerViewWeakReference) {
        RockerView rockerView = rockerViewWeakReference.get();
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
        rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_4_ROTATE_45, this);
    }

    @Override
    public void onStart() {
        LogUtils.i("onStart");
    }

    @Override
    public void direction(RockerView.Direction direction) {
        LogUtils.i("direction=" + direction);
        mDirection = direction;
        mClickCount = 0;
        isStart = true;
        mMoveRunnable = new MoveRunnable();
        ThreadPoolManager.getInstance().execute(mMoveRunnable);
    }

    @Override
    public void onFinish() {
        LogUtils.i("onFinish");
        isStart = false;
        ThreadPoolManager.getInstance().cancel(mMoveRunnable);
    }

    private void controlMove(RockerView.Direction direction) {
        switch (direction) {
            case DIRECTION_LEFT:
                SlamManager.getInstance().moveBy(MoveDirection.TURN_LEFT);
                break;
            case DIRECTION_RIGHT:
                SlamManager.getInstance().moveBy(MoveDirection.TURN_RIGHT);
                break;
            case DIRECTION_UP:
                SlamManager.getInstance().moveBy(MoveDirection.FORWARD);
                break;
            case DIRECTION_DOWN:
                SlamManager.getInstance().moveBy(MoveDirection.BACKWARD);
                break;
            default:
                break;
        }
    }

    private class MoveRunnable implements Runnable {
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

            LogUtils.i("cancel");
            SlamManager.getInstance().cancelMove();
        }
    }
}
