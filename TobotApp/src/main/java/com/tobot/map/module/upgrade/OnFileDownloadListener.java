package com.tobot.map.module.upgrade;

/**
 * @author houdeming
 * @date 2022/12/02
 */
public interface OnFileDownloadListener {
    /**
     * 下载进度
     *
     * @param progress
     */
    void onFileDownloadProgress(int progress);

    /**
     * 下载失败
     *
     * @param msg
     */
    void onFileDownloadFail(String msg);

    /**
     * 下载完成
     */
    void onFileDownloadFinish();
}
