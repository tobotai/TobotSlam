package com.tobot.map.util;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;

import java.io.File;
import java.util.List;

/**
 * 扫描SD卡进行更新，避免android操作文件后，连接电脑后显示不更新问题（只对文件有效，文件夹无效）
 *
 * @author houdeming
 * @date 2019/9/17
 */
public class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mConnection;
    private String mFilePath;
    private String[] mFilePathArray;
    private int mScanCount;

    public MediaScanner() {
    }

    @Override
    public void onMediaScannerConnected() {
        Logger.i(BaseConstant.TAG, "onMediaScannerConnected()");
        if (mConnection != null) {
            // 扫描单个文件
            if (!TextUtils.isEmpty(mFilePath)) {
                mScanCount = 1;
                mConnection.scanFile(mFilePath, null);
                return;
            }

            // 扫描多个文件
            if (mFilePathArray != null && mFilePathArray.length > 0) {
                mScanCount = mFilePathArray.length;
                for (String path : mFilePathArray) {
                    mConnection.scanFile(path, null);
                }
            }
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        Logger.i(BaseConstant.TAG, "onScanCompleted()");
        // 该方法会多次回调，完成后要断开连接
        mScanCount--;
        if (mScanCount <= 0) {
            Logger.i(BaseConstant.TAG, "MediaScanner disconnect");
            if (mConnection != null) {
                mConnection.disconnect();
            }
        }
    }

    public void scanFile(Context context, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            connect(context, filePath, null);
        }
    }

    public void scanFile(Context context, String[] filePaths) {
        if (filePaths != null && filePaths.length > 0) {
            connect(context, "", filePaths);
        }
    }

    public void scanFile(Context context, List<String> filePaths) {
        if (filePaths != null && !filePaths.isEmpty()) {
            connect(context, "", filePaths.toArray(new String[0]));
        }
    }

    public void scanFileByBroadcast(Context context, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(new File(filePath)));
            context.sendBroadcast(intent);
        }
    }

    private void connect(Context context, String filePath, String[] filePaths) {
        mFilePath = filePath;
        mFilePathArray = filePaths;
        mConnection = new MediaScannerConnection(context, this);
        mConnection.connect();
    }
}
