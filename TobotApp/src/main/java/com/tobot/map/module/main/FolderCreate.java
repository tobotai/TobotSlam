package com.tobot.map.module.main;

import android.content.Context;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.util.DateTool;
import com.tobot.map.util.FileUtils;
import com.tobot.map.util.ThreadPoolManager;

import java.io.File;

/**
 * @author houdeming
 * @date 2020/5/7
 */
class FolderCreate {
    /**
     * upgrade
     */
    private static final String FOLDER_UPGRADE = BaseConstant.DIRECTORY + "/" + BaseConstant.DIRECTORY_APK_SECOND;
    /**
     * map
     */
    private static final String FOLDER_MAP = BaseConstant.DIRECTORY + "/" + BaseConstant.DIRECTORY_MAP_SECOND;
    /**
     * log
     */
    private static final String FOLDER_LOG = BaseConstant.DIRECTORY + "/" + BaseConstant.DIRECTORY_LOG_SECOND;
    /**
     * firmware
     */
    private static final String FOLDER_FIRMWARE = BaseConstant.DIRECTORY + "/" + BaseConstant.DIRECTORY_FIRMWARE_SECOND;

    FolderCreate(final Context context) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                createFolder(FileUtils.getFolder(context, FOLDER_UPGRADE));
                createFolder(FileUtils.getFolder(context, FOLDER_MAP));
                createFolder(FileUtils.getFolder(context, FOLDER_LOG));
                createFolder(FileUtils.getFolder(context, FOLDER_FIRMWARE));
                // 刷新log文件
                refreshLogFile(context);
            }
        });
    }

    private void createFolder(String path) {
        File file = new File(path);
        // 判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            // 通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            file.mkdirs();
        }
    }

    private void refreshLogFile(Context context) {
        try {
            File dir = new File(BaseConstant.getLogDirectory(context));
            if (!dir.exists()) {
                return;
            }

            File[] fileArray = dir.listFiles();
            if (fileArray != null && fileArray.length > 0) {
                // log文件保存只保留最近7天的
                String time = DateTool.getDesignatedTime(-7);
                Logger.i(BaseConstant.TAG, "time=" + time);
                for (File file : fileArray) {
                    String name = file.getName();
                    name = name.substring(0, name.lastIndexOf("-"));
                    if (DateTool.isBeforeDate(name, time)) {
                        String path = file.getAbsolutePath();
                        Logger.i(BaseConstant.TAG, "delete path=" + path);
                        FileUtils.deleteFile(file.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.i(BaseConstant.TAG, "error=" + e.getMessage());
        }
    }
}
