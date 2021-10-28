package com.tobot.map.module.net;

/**
 * @author houdeming
 * @date 2018/7/21
 */
public interface HttpResultCallback<T> {
    /**
     * 网络请求结果
     *
     * @param data
     */
    void onHttpRequestResult(T data);
}
