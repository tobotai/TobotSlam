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
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static boolean isRequestPermission(Activity activity) {
        return isRequestPermission(activity, REQUESTED_PERMISSIONS[0]) || isRequestPermission(activity, REQUESTED_PERMISSIONS[1])
                || isRequestPermission(activity, REQUESTED_PERMISSIONS[2]);
    }

    private static boolean isRequestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, REQUESTED_PERMISSIONS, 0);
            return true;
        }
        return false;
    }
}
