package com.tobot.map.module.main;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;

/**
 * @author houdeming
 * @date 2022/06/15
 */
public class ReconnectSlamThread extends Thread {
    private static final long TIME_CONNECT = 1500;
    private final OnReconnectResultListener mListener;
    private boolean isStart;

    public ReconnectSlamThread(OnReconnectResultListener listener) {
        Logger.i(BaseConstant.TAG, "slam reconnect");
        mListener = listener;
        isStart = true;
    }

    @Override
    public void run() {
        super.run();
        int tryCount = 0;
        int maxCount = 10;
        while (isStart) {
            try {
                int connectResult = SlamManager.getInstance().connect();
                Logger.i(BaseConstant.TAG, "slam reconnectResult=" + connectResult);
                if (connectResult == SlamCode.SUCCESS) {
                    if (isStart) {
                        callbackResult(true);
                    }
                    return;
                }

                // 避免一直不停的重连
                if (tryCount >= maxCount) {
                    callbackResult(false);
                    return;
                }

                tryCount++;
                Thread.sleep(TIME_CONNECT);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void close() {
        isStart = false;
        interrupt();
    }

    private void callbackResult(boolean isSuccess) {
        if (mListener != null) {
            mListener.onReconnectResult(isSuccess);
        }
    }

    public interface OnReconnectResultListener {
        /**
         * 重连结果
         *
         * @param isSuccess
         */
        void onReconnectResult(boolean isSuccess);
    }
}
