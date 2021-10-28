package com.tobot.map.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

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

    public static String getVersion(Context context, String packageName) {
        PackageInfo info = getPackageInfo(context, packageName);
        return info != null ? info.versionName : "";
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
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
            } else {
                uri = Uri.fromFile(file);
            }

            intent.setDataAndType(uri, "application/vnd.android.package-archive");
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
