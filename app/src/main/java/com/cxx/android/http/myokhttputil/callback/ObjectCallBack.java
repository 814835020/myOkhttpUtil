package com.cxx.android.http.myokhttputil.callback;

import android.os.Handler;

import com.cxx.android.http.myokhttputil.Log.L;
import com.cxx.android.http.myokhttputil.OkhttpUtil;
import com.cxx.android.http.myokhttputil.util.ReflectionUtil;
import com.cxx.android.http.myokhttputil.parse.Parse;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by idea on 16/5/10.
 * <p/>
 * 解析出对象来，并将对象返回
 */
public abstract class ObjectCallBack<T> implements Callback {

    // 解析类
    static private Parse mParse;
    // 主线程 handler
    private Handler mMainHandler;

    public ObjectCallBack() {
        mParse = OkhttpUtil.getPares();
        mMainHandler = OkhttpUtil.getMainHandler();
    }

    /**
     * 当执行完onFailure和onResponse后调用，在UI线程中执行
     *
     * @param isOk
     * @param obj
     * @param msg  某些可能需要的信息，比如报错信息
     */
    abstract public void onAfter(boolean isOk, T obj, String msg);

    public String onFailed(Call call, IOException e) {
        return "";
    }

    public String onResponsed(Call call, Response response, T obj) {
        return "";
    }

    @Override
    public void onFailure(Call call, IOException e) {
        // 网络请求失败
        L.d("接口:" + call.request().tag().toString() + "\n" + e.getMessage());
        onAfterRunOnMainThread(false, null, onFailed(call, e));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        String resutlInfo = response.body().string();
        L.d("返回接口名称：" + call.request().tag());
        if (response.isSuccessful()) {
            // 打印json
            L.json(resutlInfo);

            Class<T> clazz = null;
            Type[] parameterizedTypes = ReflectionUtil.getParameterizedTypes(this);

            try {
                clazz = (Class<T>) ReflectionUtil.getClass(parameterizedTypes[0]);
            } catch (ClassNotFoundException e) {
                L.e("泛型反射过程异常！！！");
                e.printStackTrace();
            }

            T resultObject = mParse.parseReuslt(resutlInfo, clazz);
            if (null != resultObject) {
                onAfterRunOnMainThread(true, resultObject, onResponsed(call, response, resultObject));
            } else {
                onAfterRunOnMainThread(false, null, "接口：" + call.request().tag() + ",JSON解析异常");
            }
        } else {
            L.e("responseCode: "+response.code());
            onAfterRunOnMainThread(false, null, "网络请求失败，responseCode：" + response.code());
        }
    }

    // onAfter运行在UI线程中
    private void onAfterRunOnMainThread(final boolean isOk, final T obj, final String msg) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onAfter(isOk, obj, msg);
            }
        });
    }
}
