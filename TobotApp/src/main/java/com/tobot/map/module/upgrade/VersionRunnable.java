package com.tobot.map.module.upgrade;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author houdeming
 * @date 2019/7/19
 */
public class VersionRunnable implements Runnable {
    private Context mContext;
    private Response<ResponseBody> mResponse;
    private OnDownloadListener mOnDownloadListener;

    public VersionRunnable(@NonNull WeakReference<Context> contextReference, @NonNull WeakReference<Response<ResponseBody>> responseReference,
                           @NonNull WeakReference<OnDownloadListener> listenerReference) {
        mContext = contextReference.get();
        mResponse = responseReference.get();
        mOnDownloadListener = listenerReference.get();
    }

    @Override
    public void run() {
        Logger.i(BaseConstant.TAG, "begin parse txt file");
        AppBean appBean = null;
        if (mResponse != null) {
            String content = readTxt(mResponse.body());
            Logger.i(BaseConstant.TAG, "content=" + content);
            AppBean bean = parseResult(content);
            if (bean != null) {
                int netCode = bean.getAppCode();
                Logger.i(BaseConstant.TAG, "netCode=" + netCode);
                if (netCode > getLocalVersionCode(mContext)) {
                    appBean = bean;
                }
            }
        }

        if (mOnDownloadListener != null) {
            mOnDownloadListener.onDownload(appBean);
        }
    }

    private String readTxt(ResponseBody body) {
        String content = "";
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        if (body != null) {
            try {
                inputStream = body.byteStream();
                if (inputStream != null) {
                    inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder builder = new StringBuilder();
                    String line;
                    // 按行读取
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }

                    content = builder.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }

                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return content;
    }

    private AppBean parseResult(String content) {
        AppBean bean = null;
        if (!TextUtils.isEmpty(content)) {
            try {
                JSONObject object = new JSONObject(content);
                bean = new AppBean();
                bean.setAppName(object.optString("appName"));
                bean.setAppPackageName(object.optString("appPackageName"));
                bean.setAppCode(object.optInt("appCode"));
                bean.setUrl(object.optString("url"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bean;
    }

    private long getLocalVersionCode(Context context) {
        long versionCode = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Logger.i(BaseConstant.TAG, "localVersionCode=" + versionCode);
        return versionCode;
    }
}
