package com.tobot.map.module.net;

import android.support.annotation.NonNull;

import com.tobot.map.BuildConfig;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.net.retrofit.RetrofitHelper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author houdeming
 * @date 2018/7/20
 */
public class HttpApi {
    private static HttpListener sListener;

    static {
        sListener = RetrofitHelper.create(BuildConfig.BASE_URL, HttpListener.class);
    }

    public static void queryVersion(String url, final HttpResultCallback<Response<ResponseBody>> callback) {
        if (sListener != null) {
            sListener.downloadFile(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Logger.i(BaseConstant.TAG, "queryVersion onResponse()");
                    if (callback != null) {
                        callback.onHttpRequestResult(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Logger.i(BaseConstant.TAG, "queryVersion() onFailure() error=" + throwable.getMessage());
                    if (callback != null) {
                        callback.onHttpRequestResult(null);
                    }
                }
            });
        }
    }

    public static void downloadFile(String url, final HttpResultCallback<Response<ResponseBody>> callback) {
        if (sListener != null) {
            sListener.downloadFile(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Logger.i(BaseConstant.TAG, "downloadFile onResponse()");
                    if (callback != null) {
                        callback.onHttpRequestResult(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Logger.i(BaseConstant.TAG, "downloadFile() onFailure() error=" + throwable.getMessage());
                    if (callback != null) {
                        callback.onHttpRequestResult(null);
                    }
                }
            });
        }
    }
}
