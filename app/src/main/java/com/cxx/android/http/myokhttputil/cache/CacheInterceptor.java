package com.cxx.android.http.myokhttputil.cache;


import android.content.Context;

import com.cxx.android.http.myokhttputil.util.HttpUtils;
import com.cxx.android.http.myokhttputil.util.LogUtil;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by idea on 16/5/13.
 * 缓存拦截器,没有网络的时候取缓存
 */
public class CacheInterceptor implements Interceptor {

    private Context mContext;

    public CacheInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!HttpUtils.isNetworkStatusOK(mContext.getApplicationContext())) {
            LogUtil.d("无网络请求");
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }

        Response response = chain.proceed(request);

        if (HttpUtils.isNetworkStatusOK(mContext.getApplicationContext())) {
            int maxAge = 0 * 60; // 有网络时 设置缓存超时时间0个小时
            response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                    .build();
        } else {
            int maxStale = 60 * 60 * 24 * 7; // 无网络时，设置超时为1周
            response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .removeHeader("Pragma")
                    .build();

        }
        return response;
    }
}
