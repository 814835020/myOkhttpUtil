package com.cxx.android.http.myokhttputil.callback;

import android.os.Handler;

import com.cxx.android.http.myokhttputil.Log.MyLog;
import com.cxx.android.http.myokhttputil.OkhttpUtil;
import com.cxx.android.http.myokhttputil.parse.Parse;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by idea on 16/5/10.
 * <p/>
 * 解析出对象字典来，并将对象字典返回，泛型类建议使用object
 */
public abstract class MapCallBack<T> implements Callback {

    // 解析类
    static private Parse mParse;
    // 主线程 handler
    private Handler mMainHandler;

    public MapCallBack() {
        mParse = OkhttpUtil.getPares();
        mMainHandler = OkhttpUtil.getMainHandler();
    }

    /**
     * 当执行完onFailure和onResponse后调用，在UI线程中执行
     *
     * @param isOk
     * @param map
     * @param msg  某些可能需要的信息，比如报错信息
     */
    abstract public void onAfter(boolean isOk, Map<String, T> map, String msg);

    public String onFailed(Call call, IOException e) {
        return "";
    }

    public String onResponsed(Call call, Response response, Map<String, T> map) {
        return "";
    }

    @Override
    public void onFailure(Call call, IOException e) {
        // 网络请求失败
        MyLog.e("接口:" + call.request().tag().toString() + "\n" + e.getMessage());
        onAfterRunOnMainThread(false, null, onFailed(call, e));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        String resutlInfo = response.body().string();
        MyLog.debug("返回接口名称：" + call.request().tag());
        if (response.isSuccessful()) {
            // 打印json
            MyLog.json(resutlInfo);

            Map<String, T> resultMap = mParse.parseDictionary(resutlInfo);

            if (null != resultMap) {
                onAfterRunOnMainThread(true, resultMap, onResponsed(call, response, resultMap));
            } else {
                onAfterRunOnMainThread(false, null, "接口：" + call.request().tag() + ",JSON解析异常");
            }
        } else {
            MyLog.e("responseCode: "+response.code());
            onAfterRunOnMainThread(false, null, "网络请求失败，responseCode：" + response.code());
        }
    }

    // onAfter运行在UI线程中
    private void onAfterRunOnMainThread(final boolean isOk, final Map<String, T> map, final String msg) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onAfter(isOk, map, msg);
            }
        });
    }
}
