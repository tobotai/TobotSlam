package com.tobot.map.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * 捕获全局导常
 *
 * @author houdeming
 * @date 2019/5/21
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();
    private Context mContext;

    private CrashHandler() {
    }

    private static class CrashHandlerHolder {
        private static final CrashHandler INSTANCE = new CrashHandler();
    }

    public static CrashHandler getInstance() {
        return CrashHandlerHolder.INSTANCE;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.i(TAG, "errorMsg=" + e.getMessage());
        exportExceptionToSDCard(e);
        e.printStackTrace();
        // 结束当前应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 输出异常信息到SD卡
     *
     * @param throwable
     */
    private void exportExceptionToSDCard(Throwable throwable) {
        // 判断SD卡是否存在
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        String fileName = getFileName();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            PrintWriter pw = new PrintWriter(writer);
            pw.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            pw.println(getDeviceInfo());
            throwable.printStackTrace(pw);
            writer.close();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileName() {
        String directory = Environment.getExternalStorageDirectory().getAbsolutePath().concat(File.separator).concat("crashLog");
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            // 避免文件输出的数量过多，所以使用包名命名，只保存一份异常文件
            return directory.concat(File.separator).concat(pi.packageName).concat(".log");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取设备信息
     *
     * @return
     */
    private String getDeviceInfo() {
        String result = "";
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            StringBuilder sb = new StringBuilder();
            // 包名
            sb.append("package: ");
            sb.append(pi.packageName);
            sb.append("\n");
            // App版本
            sb.append("App Version: ");
            sb.append(pi.versionName);
            sb.append("_");
            sb.append(pi.versionCode);
            sb.append("\n");
            // Android版本号
            sb.append("OS Version: ");
            sb.append(Build.VERSION.RELEASE);
            sb.append("_");
            sb.append(Build.VERSION.SDK_INT);
            sb.append("\n");
            // 手机制造商
            sb.append("Vendor: ");
            sb.append(Build.MANUFACTURER);
            sb.append("\n");
            // 手机型号
            sb.append("Model: ");
            sb.append(Build.MODEL);
            sb.append("\n");
            // CPU架构
            sb.append("CPU: ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sb.append(Arrays.toString(Build.SUPPORTED_ABIS));
            } else {
                sb.append(Build.CPU_ABI);
            }
            result = sb.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
}
