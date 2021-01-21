package com.tobot.map.module.main;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.tobot.map.event.ConnectSlamEvent;
import com.tobot.map.util.LogUtils;
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
public class MapService extends Service {
    private ConnectSlamRunnable mConnectSlamRunnable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
        EventBus.getDefault().register(this);
        new FolderCreate(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
        if (mConnectSlamRunnable != null) {
            mConnectSlamRunnable.close();
            mConnectSlamRunnable = null;
        }
        SlamManager.getInstance().disconnect();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectSlamEvent(ConnectSlamEvent event) {
        if (mConnectSlamRunnable == null) {
            mConnectSlamRunnable = new ConnectSlamRunnable(new WeakReference<>(this), event.getIp());
            ThreadPoolManager.getInstance().execute(mConnectSlamRunnable);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
                boolean isConnect = NetworkUtils.isConnected(MapService.this);
                LogUtils.i("net isConnect=" + isConnect);
            }
        }
    };
}
