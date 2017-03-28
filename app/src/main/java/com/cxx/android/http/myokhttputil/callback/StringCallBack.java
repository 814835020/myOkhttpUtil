package com.cxx.android.http.myokhttputil.callback;

import android.os.Handler;

import com.cxx.android.http.myokhttputil.util.HttpUtils;
import com.cxx.android.http.myokhttputil.util.LogUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by idea on 16/5/10.
 * <p/>
 * 不解析，直接返回返回的数据
 */
public abstract class StringCallBack implements Callback {

    // 主线程 handler
    private Handler mMainHandler;

    public StringCallBack() {
        mMainHandler = HttpUtils.getMainHandler();
    }

    /**
     * 当执行完onFailure和onResponse后调用，在UI线程中执行
     *
     * @param isOk
     * @param str
     * @param msg  某些可能需要的信息，比如报错信息
     */
    abstract public void onAfter(boolean isOk, String str, String msg);

    public String onFailed(Call call, IOException e) {
        return "";
    }

    public String onResponsed(Call call, Response response) {
        return "";
    }

    @Override
    public void onFailure(Call call, IOException e) {
        // 网络请求失败
        LogUtil.e("接口:" + call.request().tag().toString() + "\n" + e.getMessage());
        onAfterRunOnMainThread(false, null, onFailed(call, e));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        String resutlInfo = response.body().string();
        LogUtil.d("返回接口名称：" + call.request().tag());
        if (response.isSuccessful()) {
            // 打印json
            LogUtil.json(resutlInfo);
            onAfterRunOnMainThread(true, resutlInfo, onResponsed(call, response));
        } else {
            LogUtil.e("responseCode: "+response.code());
            onAfterRunOnMainThread(false, null, "网络请求失败，responseCode：" + response.code());
        }
    }

    // onAfter运行在UI线程中
    private void onAfterRunOnMainThread(final boolean isOk, final String resultInfo, final String msg) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onAfter(isOk, resultInfo, msg);
            }
        });
    }
}
