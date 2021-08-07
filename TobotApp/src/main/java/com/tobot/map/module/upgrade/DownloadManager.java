package com.tobot.map.module.upgrade;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.net.HttpApi;
import com.tobot.map.module.net.HttpResultCallBack;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.map.util.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author houdeming
 * @date 2018/7/4
 */
public class DownloadManager {
    private static final int MSF_UPDATE_PROGRESS = 1;
    private static final int MSF_DOWNLOAD_FAIL = 2;
    private static final int MSF_DOWNLOAD_COMPLETE = 3;
    private static final int NOTIFY_ID = 1;
    private Context mContext;
    private MainHandler mMainHandler;
    @SuppressLint("StaticFieldLeak")
    private static DownloadManager sInstance;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    /**
     * 定义个更新速率，避免更新通知栏过于频繁导致卡顿
     */
    private float rate = 0.0f;

    private DownloadManager(Context context) {
        mContext = context;
        mMainHandler = new MainHandler(new WeakReference<>(this));
    }

    public static DownloadManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public void checkUpdate(String url, final OnDownloadListener listener) {
        HttpApi.queryVersion(url, new HttpResultCallBack<Response<ResponseBody>>() {
            @Override
            public void onHttpRequestResult(retrofit2.Response<ResponseBody> data) {
                if (data != null) {
                    ThreadPoolManager.getInstance().execute(new VersionRunnable(new WeakReference<>(mContext), new WeakReference<>(data), new WeakReference<>(listener)));
                    return;
                }

                if (listener != null) {
                    listener.onDownload(null);
                }
            }
        });
    }

    public void downLoadApk(String url) {
        Logger.i(BaseConstant.TAG, "url=" + url);
        setNotification();
        HttpApi.downloadFile(url, new HttpResultCallBack<Response<ResponseBody>>() {
            @Override
            public void onHttpRequestResult(Response<ResponseBody> data) {
                if (data != null) {
                    ThreadPoolManager.getInstance().execute(new FileRunnable(new WeakReference<>(data.body())));
                    return;
                }

                if (mMainHandler != null) {
                    mMainHandler.obtainMessage(MSF_DOWNLOAD_FAIL, mContext.getString(R.string.download_fail_tips)).sendToTarget();
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setNotification() {
        // 显示状态栏下载通知
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentTitle(mContext.getString(R.string.tv_apk_download_begin))
                .setContentText(mContext.getString(R.string.tv_apk_content_tips))
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher))
                .setOngoing(true)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    private class FileRunnable implements Runnable {
        private ResponseBody mResponseBody;

        private FileRunnable(@NonNull WeakReference<ResponseBody> reference) {
            mResponseBody = reference.get();
        }

        @Override
        public void run() {
            if (mResponseBody == null) {
                if (mMainHandler != null) {
                    mMainHandler.obtainMessage(MSF_DOWNLOAD_FAIL, mContext.getString(R.string.download_fail_tips)).sendToTarget();
                }
                return;
            }

            Logger.i(BaseConstant.TAG, "writeFile()");
            writeFile(mResponseBody);
        }

        private void writeFile(ResponseBody responseBody) {
            InputStream inputStream = null;
            byte[] buff = new byte[2048];
            int len;
            FileOutputStream fos = null;
            try {
                File file = getApkFile();
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                if (file.exists()) {
                    file.delete();
                }

                inputStream = responseBody.byteStream();
                long total = responseBody.contentLength();

                fos = new FileOutputStream(file);
                long sum = 0;
                while ((len = inputStream.read(buff)) != -1) {
                    fos.write(buff, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / total * 100);
                    if (rate != progress) {
                        if (mMainHandler != null) {
                            mMainHandler.obtainMessage(MSF_UPDATE_PROGRESS, progress, 0).sendToTarget();
                        }
                        rate = progress;
                    }
                }

                fos.flush();
                if (mMainHandler != null) {
                    mMainHandler.sendEmptyMessage(MSF_DOWNLOAD_COMPLETE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.i(BaseConstant.TAG, "error=" + e.getMessage());
                if (mMainHandler != null) {
                    mMainHandler.obtainMessage(MSF_DOWNLOAD_FAIL, mContext.getString(R.string.permission_sd_card_open_tips)).sendToTarget();
                }
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class MainHandler extends Handler {
        private DownloadManager mManager;

        private MainHandler(WeakReference<DownloadManager> reference) {
            super(Looper.getMainLooper());
            mManager = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSF_UPDATE_PROGRESS:
                    if (mManager != null) {
                        mManager.updateProgress(msg.arg1);
                    }
                    break;
                case MSF_DOWNLOAD_FAIL:
                    if (mManager != null) {
                        mManager.downloadFail((String) msg.obj);
                    }
                    break;
                case MSF_DOWNLOAD_COMPLETE:
                    if (mManager != null) {
                        mManager.downloadComplete();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void updateProgress(int progress) {
        mBuilder.setContentTitle(mContext.getString(R.string.tv_apk_download_tips))
                .setContentText(String.format(Locale.CHINESE, "%d%%", progress))
                .setProgress(100, progress, false)
                .setWhen(System.currentTimeMillis());
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFY_ID, notification);
    }

    private void downloadFail(String msg) {
        ToastUtils.getInstance(mContext).show(msg);
        mNotificationManager.cancel(NOTIFY_ID);
    }

    private void downloadComplete() {
        Logger.i(BaseConstant.TAG, "downloadComplete()");
        Intent intent = installIntent(getApkFile());
        if (intent == null) {
            return;
        }

        boolean isOnFront = onFront();
        Logger.i(BaseConstant.TAG, "DOWNLOAD_COMPLETE isOnFront=" + isOnFront);
        if (isOnFront) {
            mNotificationManager.cancel(NOTIFY_ID);
            int sdkInt = Build.VERSION.SDK_INT;
            Logger.i(BaseConstant.TAG, "Build.VERSION.SDK_INT=" + sdkInt);
            if (sdkInt >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            mContext.startActivity(intent);
            return;
        }

        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.tv_apk_download_complete))
                .setProgress(0, 0, false)
                .setDefaults(Notification.DEFAULT_ALL);
        Notification notification2 = mBuilder.build();
        notification2.flags = Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFY_ID, notification2);
    }

    private File getApkFile() {
        String apkFilePath = BaseConstant.getApkDirectory(mContext).concat(File.separator).concat(BaseConstant.APK_NAME);
        return new File(apkFilePath);
    }

    /**
     * 是否运行在用户前面
     */
    private boolean onFront() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null || appProcesses.isEmpty()) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(mContext.getPackageName()) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    /**
     * 安装 7.0 以上记得配置 fileProvider
     *
     * @param file
     */
    private Intent installIntent(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileProvider", file);
            } else {
                uri = Uri.fromFile(file);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
