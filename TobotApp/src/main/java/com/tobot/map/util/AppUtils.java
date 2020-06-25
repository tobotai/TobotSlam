package com.tobot.map.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.File;

/**
 * @author houdeming
 * @date 2019/7/22
 */
public class AppUtils {

    public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static Drawable getAppIcon(Context context, String packageName) {
        Drawable drawable = null;
        try {
            drawable = context.getPackageManager().getApplicationIcon(packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    /**
     * 安装apk
     *
     * @param context
     * @param apkFilePath
     */
    public static void installApk(Context context, String apkFilePath) {
        File file = new File(apkFilePath);
        if (file.exists()) {
            // 通过Intent安装APK文件
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(apkFilePath)), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    /**
     * 卸载apk
     *
     * @param context
     * @param packageName
     */
    public static void uninstallApk(Context context, String packageName) {
        PackageInfo info = getPackageInfo(context, packageName);
        if (info != null) {
            Uri uri = Uri.parse("package:" + packageName);
            Intent intent = new Intent(Intent.ACTION_DELETE, uri);
            context.startActivity(intent);
        }
    }
}
