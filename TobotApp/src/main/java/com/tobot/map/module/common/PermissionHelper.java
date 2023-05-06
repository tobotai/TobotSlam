package com.tobot.map.module.common;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * 权限请求
 *
 * @author houdeming
 * @date 2020/3/3
 */
public class PermissionHelper {
    private static final String[] REQUESTED_PERMISSIONS = {
            /*
             * 部分手机WIFI SSID获取结果为unknown，需要加定位权限
            */
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void checkPermission(Activity activity) {
        for (String permission : REQUESTED_PERMISSIONS) {
            int grant = ContextCompat.checkSelfPermission(activity, permission);
            if (grant != PackageManager.PERMISSION_GRANTED) {
                // 请求权限会执行2次onResume()方法
                ActivityCompat.requestPermissions(activity, REQUESTED_PERMISSIONS, 0);
            }
        }
    }

    public static boolean isRequestPermission(Activity activity) {
        for (String permission : REQUESTED_PERMISSIONS) {
            int grant = ContextCompat.checkSelfPermission(activity, permission);
            if (grant != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, REQUESTED_PERMISSIONS, 0);
                return true;
            }
        }

        return false;
    }
}
