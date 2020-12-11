package com.tobot.map.module.main;

import android.content.Context;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.util.FileUtils;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * @author houdeming
 * @date 2020/5/7
 */
class FolderCreate {
    /**
     * map
     */
    private static final String FOLDER_MAP = BaseConstant.DIRECTORY + "/" + BaseConstant.DIRECTORY_MAP_SECOND;

    FolderCreate(final Context context) {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                createFolder(FileUtils.getFolder(context, FOLDER_MAP));
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
}
