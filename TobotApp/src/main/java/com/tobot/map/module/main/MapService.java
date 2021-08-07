package com.tobot.map.module.main;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.tobot.map.BuildConfig;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.event.CheckEndEvent;
import com.tobot.map.event.CheckEvent;
import com.tobot.map.event.ConnectSlamEvent;
import com.tobot.map.event.DownloadEvent;
import com.tobot.map.module.log.LogRunnable;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.upgrade.AppBean;
import com.tobot.map.module.upgrade.DownloadManager;
import com.tobot.map.module.upgrade.OnDownloadListener;
import com.tobot.map.util.NetworkUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
public class MapService extends Service implements OnDownloadListener {
    private boolean isFirst;
    private ConnectSlamThread mConnectSlamThread;
    private LogRunnable mLogRunnable;
    private AppBean mUpgradeBean;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new FolderCreate(this);
        if (DataHelper.getInstance().getLogType(this) == BaseConstant.LOG_ADB) {
            mLogRunnable = new LogRunnable(this, "tobotSlam | slamLog", BaseConstant.getLogDirectory(this), BaseConstant.getAdbLogFileName(this));
            ThreadPoolManager.getInstance().execute(mLogRunnable);
        }

        isFirst = true;
        IntentFilter filter = new IntentFilter();
        // 7.0版本以上只能代码中动态注册才能接收该广播
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i(BaseConstant.TAG, "onDestroy()");
        unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
        if (mConnectSlamThread != null) {
            mConnectSlamThread.close();
            mConnectSlamThread = null;
        }

        SlamManager.getInstance().disconnect();
        if (mLogRunnable != null) {
            mLogRunnable.destroy();
        }

        Logger.save();
        Logger.close();
        // 销毁应用
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onDownload(AppBean bean) {
        mUpgradeBean = bean;
        EventBus.getDefault().post(new CheckEndEvent(bean != null));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectSlamEvent(ConnectSlamEvent event) {
        if (mConnectSlamThread == null) {
            mConnectSlamThread = new ConnectSlamThread(new WeakReference<>(this), event.getIp());
            mConnectSlamThread.start();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent event) {
        if (mUpgradeBean != null) {
            DownloadManager.getInstance(this).downLoadApk(mUpgradeBean.getUrl());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckEvent(CheckEvent event) {
        DownloadManager.getInstance(this).checkUpdate(BuildConfig.VERSION_TXT_URL, this);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
                // 第一次检测到网络的时候，要检测新版本
                boolean isConnect = NetworkUtils.isConnected(MapService.this);
                Logger.i(BaseConstant.TAG, "net isConnect=" + isConnect);
                if (isConnect) {
                    if (isFirst) {
                        isFirst = false;
                        // 打开应用检测版本
                        DownloadManager.getInstance(MapService.this).checkUpdate(BuildConfig.VERSION_TXT_URL, MapService.this);
                    }
                }
            }
        }
    };
}
