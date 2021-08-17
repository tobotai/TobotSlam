package com.tobot.map.module.log;

import android.content.Context;
import android.text.TextUtils;

import com.tobot.map.constant.BaseConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志收集
 *
 * @author houdeming
 * @date 2021/01/27
 */
public class LogRunnable implements Runnable {
    private Context mContext;
    private Process mProcess;
    private String mTag, mFileFolder, mFileName;

    public LogRunnable(Context context, String tag, String fileFolder, String fileName) {
        mContext = context;
        mTag = tag;
        mFileFolder = fileFolder;
        mFileName = fileName;
    }

    @Override
    public void run() {
        try {
            createLogCollector();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        if (mProcess != null) {
            mProcess.destroy();
        }
    }

    private void createLogCollector() {
        if (TextUtils.isEmpty(mFileFolder) || TextUtils.isEmpty(mFileName)) {
            return;
        }

        File dir = new File(mFileFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<String> commandList = new ArrayList<>();
        commandList.add("logcat");
        commandList.add("-f");
        commandList.add(mFileFolder + "/" + mFileName);
        commandList.add("-v");
        commandList.add("time");
        commandList.add(mTag + ":I");
        // 过滤所有的错误信息
        commandList.add("System.err:W");
        // 过滤所有的错误信息
        commandList.add("System.out:I");
        // 运行报错
        commandList.add("AndroidRuntime:E");
        // 过滤指定TAG的信息
        commandList.add(mTag + ":V");
        commandList.add(mTag + ":D");
        commandList.add("*:S");

        try {
            Logger.i(BaseConstant.TAG, "adbLog execute");
            mProcess = Runtime.getRuntime().exec(commandList.toArray(new String[0]));
        } catch (Exception e) {
            e.printStackTrace();
            Logger.i(BaseConstant.TAG, "adbLog error=" + e.getMessage());
        }
    }
}
