package com.tobot.map.module.main;

import android.content.Context;
import android.text.TextUtils;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.event.ConnectSuccessEvent;
import com.tobot.map.module.log.Logger;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2020/5/13
 */
public class ConnectSlamThread extends Thread {
    /**
     * 底盘端口号
     */
    private static final int PORT = 1445;
    private static final long TIME_CONNECT = 2000;
    private Context mContext;
    private String mIp;
    private boolean isStart;

    public ConnectSlamThread(WeakReference<Context> reference, String ip) {
        mContext = reference.get();
        mIp = ip;
        isStart = true;
    }

    @Override
    public void run() {
        while (isStart) {
            try {
                Thread.sleep(TIME_CONNECT);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            int connectResult;
            if (TextUtils.isEmpty(mIp)) {
                connectResult = SlamManager.getInstance().connect();
            } else {
                connectResult = SlamManager.getInstance().connect(mIp, PORT);
            }

            Logger.i(BaseConstant.TAG, "slam connectResult=" + connectResult);
            if (connectResult == SlamCode.SUCCESS && isStart) {
                isStart = false;
                // 添加ip
                addIp(mIp);
                EventBus.getDefault().post(new ConnectSuccessEvent());
                return;
            }
        }
    }

    public void close() {
        isStart = false;
        interrupt();
    }

    private void addIp(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            String content = MyDBSource.getInstance(mContext).queryIp(ip);
            if (TextUtils.isEmpty(content)) {
                MyDBSource.getInstance(mContext).insertIp(ip);
            }
        }
    }
}
