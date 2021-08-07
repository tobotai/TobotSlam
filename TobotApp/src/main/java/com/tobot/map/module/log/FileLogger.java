package com.tobot.map.module.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2021/01/28
 */
public class FileLogger extends AbstractLogger {
    private static final int MSG_WHAT = 1;
    private static final int CACHE_SIZE = 10 * 1024;
    private StringBuffer mCacheLog;
    private String mFileFolder, mFileName, packageName;
    private HandlerThread mLogThread;
    private Handler mLogHandler;
    private boolean isLogEnable = true;

    public FileLogger(Context context, String fileFolder, String fileName) {
        mFileFolder = fileFolder;
        mFileName = fileName;
        mCacheLog = new StringBuffer();
        packageName = context.getPackageName();

        mLogThread = new HandlerThread("LOG_THREAD");
        mLogThread.start();
        mLogHandler = new LogHandle(mLogThread.getLooper(), new WeakReference<>(this));
    }

    @Override
    public void setLogEnable(boolean isLogEnable) {
        this.isLogEnable = isLogEnable;
    }

    @Override
    public void save() {
        if (TextUtils.isEmpty(mFileFolder) || TextUtils.isEmpty(mFileName)) {
            return;
        }

        File dir = new File(mFileFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileWriter fileWriter = null;
        Writer mWriter = null;
        try {
            File file = new File(mFileFolder + "/" + mFileName);
            fileWriter = new FileWriter(file, true);
            mWriter = new PrintWriter(new BufferedWriter(fileWriter, 2028));
            mWriter.write(mCacheLog.toString());
            mWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mCacheLog != null) {
                    mCacheLog.setLength(0);
                }

                if (fileWriter != null) {
                    fileWriter.close();
                }

                if (mWriter != null) {
                    mWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        isLogEnable = false;
        if (mLogHandler != null) {
            mLogHandler.removeCallbacksAndMessages(null);
        }

        if (mLogThread != null) {
            mLogThread.quit();
            mLogThread = null;
        }
    }

    @Override
    public void v(String tag, String message) {
        println(VERBOSE, tag, message);
    }

    @Override
    public void d(String tag, String message) {
        println(DEBUG, tag, message);
    }

    @Override
    public void i(String tag, String message) {
        println(INFO, tag, message);
    }

    @Override
    public void w(String tag, String message) {
        println(WARN, tag, message);
    }

    @Override
    public void e(String tag, String message) {
        println(ERROR, tag, message);
    }

    private void println(int priority, String tag, String message) {
        if (!isLogEnable || mLogHandler == null) {
            return;
        }

        String processTag = android.os.Process.myPid() + "-" + android.os.Process.myTid();
        StackTraceElement element = getStackTraceElement(8);
        String logMsg = formatLogMsg(message, element.getFileName(), element.getLineNumber(), element.getMethodName());
        String content = getPrintLog(processTag, packageName, getPriorityTag(priority), tag, logMsg);
        mLogHandler.obtainMessage(MSG_WHAT, content).sendToTarget();
    }

    private void cacheLog(String content) {
        mCacheLog.append(content);
        // 当日志达到一定数量时保存一次
        if (mCacheLog.length() > CACHE_SIZE) {
            save();
        }
    }

    @SuppressLint("HandlerLeak")
    private static class LogHandle extends Handler {
        private FileLogger mLog;

        private LogHandle(@NonNull Looper looper, WeakReference<FileLogger> reference) {
            super(looper);
            mLog = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT && mLog != null) {
                mLog.cacheLog((String) msg.obj);
            }
        }
    }
}
