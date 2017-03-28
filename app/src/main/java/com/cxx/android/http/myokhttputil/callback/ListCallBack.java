package com.cxx.android.http.myokhttputil.callback;

import com.cxx.android.http.myokhttputil.OkhttpUtil;
import com.cxx.android.http.myokhttputil.parse.Parse;
import com.cxx.android.http.myokhttputil.util.HttpUtils;
import com.cxx.android.http.myokhttputil.util.LogUtil;
import com.cxx.android.http.myokhttputil.util.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by idea on 16/5/10.
 * <p/>
 * 解析出对象数组来，并将对象数组返回
 */
public abstract class ListCallBack<T> implements Callback {

    // 解析类
    static private Parse mParse;

    public ListCallBack() {
        mParse = OkhttpUtil.getPares();
    }

    /**
     * 当执行完onFailure和onResponse后调用，在UI线程中执行
     *
     * @param isOk
     * @param list
     * @param msg  某些可能需要的信息，比如报错信息
     */
    abstract public void onAfter(boolean isOk, List<T> list, String msg);

    public String onFailed(Call call, IOException e) {
        return "";
    }

    public String onResponsed(Call call, Response response, List<T> list) {
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

            Class<T> clazz = null;
            Type[] parameterizedTypes = ReflectionUtil.getParameterizedTypes(this);

            try {
                clazz = (Class<T>) ReflectionUtil.getClass(parameterizedTypes[0]);
            } catch (ClassNotFoundException e) {
                LogUtil.e("泛型反射过程异常！！！");
                e.printStackTrace();
            }

            List<T> resultList = mParse.parseArray(resutlInfo, clazz);
            if (null != resultList) {
                onAfterRunOnMainThread(true, resultList, onResponsed(call, response, resultList));
            } else {
                onAfterRunOnMainThread(false, null, "接口：" + call.request().tag() + ",JSON解析异常");
            }
        } else {
            LogUtil.e("responseCode: " + response.code());
            onAfterRunOnMainThread(false, null, "网络请求失败，responseCode：" + response.code());
        }
    }

    // onAfter运行在UI线程中
    private void onAfterRunOnMainThread(final boolean isOk, final List<T> list, final String msg) {
        HttpUtils.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                onAfter(isOk, list, msg);
            }
        });
    }
}
