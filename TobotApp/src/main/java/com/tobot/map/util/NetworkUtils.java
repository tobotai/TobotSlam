package com.tobot.map.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author houdeming
 * @date 2018/4/16
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    /**
     * 网络是否连接
     *
     * @param context
     * @return 连上wifi没有网也返回true
     */
    public static boolean isConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                return mNetworkInfo != null && mNetworkInfo.isAvailable();
            }
        }

        return false;
    }

    /**
     * WIFI是否连接
     *
     * @param context
     * @return 只有连上wifi并且有网才返回true
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo != null) {
                    NetworkInfo.State mWiFiNetworkInfo = networkInfo.getState();
                    return mWiFiNetworkInfo == NetworkInfo.State.CONNECTED;
                }
            }
        }

        return false;
    }

    /**
     * 获取连接的信号强度
     *
     * @param context
     * @return
     */
    public static int getRssi(Context context) {
        if (context != null) {
            try {
                WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (manager != null) {
                    WifiInfo info = manager.getConnectionInfo();
                    if (info != null) {
                        // [-100, 0]，其中0到-50表示信号最好，-50到-70表示信号偏差，小于-70表示最差，有可能连接不上或者掉线
                        return info.getRssi();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return -100;
    }

    /**
     * 获取连接的WiFi的名称
     *
     * @param context
     * @return
     */
    public static String getConnectWifiName(Context context) {
        if (context != null) {
            try {
                WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (manager != null) {
                    WifiInfo info = manager.getConnectionInfo();
                    if (info != null) {
                        //"@Hyatt_WiFi" 获取的ssid是带双引号的
                        String name = info.getSSID();
                        Logger.i(BaseConstant.TAG, "name=" + name);
                        if (!TextUtils.isEmpty(name)) {
                            return name.substring(1, name.length() - 1);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @SuppressLint("HardwareIds")
    public static String getMacAddress(Context context) {
        if (context != null) {
            try {
                WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (manager != null) {
                    WifiInfo info = manager.getConnectionInfo();
                    if (info != null) {
                        String macAddress = info.getMacAddress();
                        Logger.i(BaseConstant.TAG, "macAddress=" + macAddress);
                        if (!TextUtils.isEmpty(macAddress)) {
                            macAddress = macAddress.toUpperCase(Locale.ENGLISH);
                        }
                        return macAddress;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取IP地址
     *
     * @return
     */
    public static String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses(); enumeration.hasMoreElements(); ) {
                    InetAddress inetAddress = enumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toLowerCase();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isWifiEnable(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (manager != null) {
                return manager.isWifiEnabled();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static List<ScanResult> getWifiList(Context context) {
        List<ScanResult> wifiList = new ArrayList<>();
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            List<ScanResult> scanWifiList = wifiManager.getScanResults();
            if (scanWifiList != null && !scanWifiList.isEmpty()) {
                HashMap<String, Integer> signalStrength = new HashMap<>();
                for (int i = 0, size = scanWifiList.size(); i < size; i++) {
                    ScanResult scanResult = scanWifiList.get(i);
                    String sid = scanResult.SSID;
                    if (!TextUtils.isEmpty(sid)) {
                        String key = sid + " " + scanResult.capabilities;
                        if (!signalStrength.containsKey(key)) {
                            signalStrength.put(key, i);
                            wifiList.add(scanResult);
                        }
                    }
                }
            }
        }

        return wifiList;
    }
}
