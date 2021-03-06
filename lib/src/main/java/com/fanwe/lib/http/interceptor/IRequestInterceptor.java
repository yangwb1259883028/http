package com.fanwe.lib.http.interceptor;

import com.fanwe.lib.http.IRequest;
import com.fanwe.lib.http.IResponse;

/**
 * 用于请求拦截
 * Created by zhengjun on 2017/10/11.
 */
public interface IRequestInterceptor
{
    /**
     * 发起网络请求之前回调
     *
     * @param request
     */
    void beforeExecute(IRequest request);

    /**
     * 发起网络请求之后回调
     *
     * @param request
     * @param response
     */
    void afterExecute(IRequest request, IResponse response);
}
