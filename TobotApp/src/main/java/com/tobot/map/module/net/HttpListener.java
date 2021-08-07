package com.tobot.map.module.net;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author houdeming
 * @date 2018/7/20
 */
public interface HttpListener {
    /**
     * 下载文件
     * Streaming：添加这个注解用来下载大文件
     *
     * @param fileUrl
     * @return
     */
    @Streaming
    @GET()
    Call<ResponseBody> downloadFile(@Url String fileUrl);
}
