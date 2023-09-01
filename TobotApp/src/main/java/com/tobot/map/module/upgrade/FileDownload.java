package com.tobot.map.module.upgrade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.net.HttpApi;
import com.tobot.map.module.net.HttpResultCallback;
import com.tobot.map.util.ThreadPoolManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author houdeming
 * @date 2022/12/02
 */
public class FileDownload {
    private Context mContext;
    private File mPathFile;
    private OnFileDownloadListener mOnFileDownloadListener;

    private static class FileDownloadHolder {
        @SuppressLint("StaticFieldLeak")
        private static final FileDownload INSTANCE = new FileDownload();
    }

    public static FileDownload getInstance() {
        return FileDownloadHolder.INSTANCE;
    }

    public void downLoad(Context context, String url, File pathFile, OnFileDownloadListener listener) {
        mContext = context;
        mPathFile = pathFile;
        mOnFileDownloadListener = listener;
        HttpApi.downloadFile(url, new HttpResultCallback<Response<ResponseBody>>() {
            @Override
            public void onHttpRequestResult(Response<ResponseBody> data) {
                if (data != null) {
                    ThreadPoolManager.getInstance().execute(new FileRunnable(new WeakReference<>(data.body())));
                    return;
                }

                callbackDownloadResult(false, mContext.getString(R.string.download_fail));
            }
        });
    }

    private void callbackDownloadResult(boolean isSuccess, String msg) {
        if (mOnFileDownloadListener != null) {
            if (isSuccess) {
                mOnFileDownloadListener.onFileDownloadFinish();
                return;
            }

            mOnFileDownloadListener.onFileDownloadFail(msg);
        }
    }

    private void callbackProgress(int progress) {
        if (mOnFileDownloadListener != null) {
            mOnFileDownloadListener.onFileDownloadProgress(progress);
        }
    }

    private class FileRunnable implements Runnable {
        private final ResponseBody mResponseBody;

        private FileRunnable(@NonNull WeakReference<ResponseBody> reference) {
            mResponseBody = reference.get();
        }

        @Override
        public void run() {
            Logger.i(BaseConstant.TAG, "download write thread");
            if (mResponseBody == null) {
                callbackDownloadResult(false, mContext.getString(R.string.download_fail));
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
                File file = mPathFile;
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
                // 避免更新过于频繁导致卡顿
                int lastProgress = -1;
                long sum = 0;
                while ((len = inputStream.read(buff)) != -1) {
                    fos.write(buff, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / total * 100);
                    if (lastProgress != progress) {
                        lastProgress = progress;
                        callbackProgress(progress);
                    }
                }

                fos.flush();
                callbackDownloadResult(true, "");
            } catch (Exception e) {
                e.printStackTrace();
                String error = e.getMessage();
                Logger.i(BaseConstant.TAG, "error=" + error);
                callbackDownloadResult(false, error);
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
}
