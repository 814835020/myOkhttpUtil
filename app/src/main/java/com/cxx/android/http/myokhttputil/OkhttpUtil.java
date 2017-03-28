package com.cxx.android.http.myokhttputil;

import android.content.Context;
import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.cxx.android.http.myokhttputil.Log.LogInterface;
import com.cxx.android.http.myokhttputil.cache.CacheInterceptor;
import com.cxx.android.http.myokhttputil.cookie.CookieJarImpl;
import com.cxx.android.http.myokhttputil.cookie.MemoryCookieStore;
import com.cxx.android.http.myokhttputil.https.HttpsUtils;
import com.cxx.android.http.myokhttputil.parse.Parse;
import com.cxx.android.http.myokhttputil.parse.ParseFastJson;
import com.cxx.android.http.myokhttputil.util.LogUtil;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.cxx.android.http.myokhttputil.util.LogUtil.json;

/**
 * Created by cxx on 16/5/10.
 * <p/>
 * 该工具类，目前只支持post请求，支持字典类型上传，字串类型上传，自动管理cookies，支持https，支持文件下载，不支持上传功能
 * <p/>
 * 只支持异步请求回调
 * <p/>
 * 支持缓存
 */
public class OkhttpUtil {

    private static Context mApplication;
    private static LogInterface mLog;
    private static boolean isDebug = true;

    private static int mConnectTimeout = 10;
    private static int mWriteTimeout = 10;
    private static int mReadTimeout = 10;
    // 默认缓存大小10M
    private static int mCacheSize = 10 * 10 * 1024;
    private static String mCacheName = "responses";

    private static OkHttpClient mOkHttpClient;

    // 返回字符串解析对象
    private static Parse mParse;

    public OkhttpUtil setConnectTimeout(int connectTimeout) {
        this.mConnectTimeout = connectTimeout;
        return this;
    }

    public OkhttpUtil setWriteTimeout(int writeTimeout) {
        this.mWriteTimeout = writeTimeout;
        return this;
    }

    public OkhttpUtil setReadTimeout(int readTimeout) {
        this.mReadTimeout = readTimeout;
        return this;
    }

    public OkhttpUtil setCacheSize(int cacheSize) {
        mCacheSize = cacheSize;
        return this;
    }

    public OkhttpUtil setLog(LogInterface log) {
        mLog = log;
        return this;
    }

    /**
     * 使用前需要注入json解析器
     *
     * @param parse
     */
    public OkhttpUtil setParse(Parse parse) {
        mParse = parse;
        return this;
    }

    public void init(Context context) {

        mApplication = context.getApplicationContext();

        if (null == mParse)
            mParse = new ParseFastJson();
        if (null == mLog)
            LogUtil.setLog(mLog);
        LogUtil.isDebug(isDebug);

        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(mConnectTimeout, TimeUnit.SECONDS)
                .writeTimeout(mWriteTimeout, TimeUnit.SECONDS)
                .readTimeout(mReadTimeout, TimeUnit.SECONDS)
                // 默认将cookies保存到内存中
                .cookieJar(new CookieJarImpl(new MemoryCookieStore()))
                .build();
    }

    /**
     * 返回json解析对象
     */
    public static Parse getPares() {
        Verify();
        return mParse;
    }

    /**
     * 添加离线缓存功能
     */
    static public void setCanCache() {
        //设置缓存路径
        File mHttpCacheDirectory = new File(mApplication.getExternalCacheDir(), mCacheName);
        Cache cache = new Cache(mHttpCacheDirectory, mCacheSize);
        mOkHttpClient = mOkHttpClient
                .newBuilder()
                .cache(cache)
                .addInterceptor(new CacheInterceptor(mApplication))
                .build();
    }

    /**
     * https单向验证
     *
     * @param certificates
     */
    static public void setCertificates(InputStream... certificates) {
        Verify();
        SSLSocketFactory sslSocketFactory = HttpsUtils.getSslSocketFactory(certificates, null, null);
        mOkHttpClient = mOkHttpClient
                .newBuilder()
                .sslSocketFactory(sslSocketFactory)
                .build();
    }


    /**
     * https双向认证
     *
     * @param certificates
     * @param bksFile
     * @param password
     */
    public void setCertificates(InputStream[] certificates, InputStream bksFile, String password) {
        Verify();
        mOkHttpClient = mOkHttpClient
                .newBuilder()
                .sslSocketFactory(HttpsUtils.getSslSocketFactory(certificates, bksFile, password))
                .build();
    }

    /**
     * https通信中会使用到
     *
     * @param hostNameVerifier
     */
    public void setHostNameVerifier(HostnameVerifier hostNameVerifier) {
        Verify();
        mOkHttpClient = mOkHttpClient.newBuilder()
                .hostnameVerifier(hostNameVerifier)
                .build();
    }

    /**
     * 以json键值对的方式发送数据的网络请求
     *
     * @param url
     * @param mapInfo
     * @param tag     建议使用接口名
     * @return
     */
    public static Request getMapRequest(@NonNull String url, @NonNull Map<String, String> mapInfo, Map<String, String> headerMap, @NonNull String tag) {
        Verify();
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> m : mapInfo.entrySet()) {
            builder.add(m.getKey(), m.getValue());
        }
        RequestBody formBody = builder.build();

        Request.Builder rBuilder = new Request.Builder()
                .url(url)
                .post(formBody)
                .tag(tag);

        if (null != headerMap)
            for (Map.Entry<String, String> m : headerMap.entrySet()) {
                rBuilder.addHeader(m.getKey(), m.getValue());
            }

        Request request = rBuilder.build();

        String sendJson = JSON.toJSONString(mapInfo);
        LogUtil.d("请求接口名称：" + tag);
        json(sendJson);
        return request;
    }

    /**
     * 直接发送String字符串的网络请求
     *
     * @param url
     * @param info
     * @param tag  建议使用接口名
     * @return
     */
    public static Request getStringRequest(@NonNull String url, @NonNull String info, @NonNull String tag) {
        Verify();
        MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");
        RequestBody stringBody = RequestBody.create(MEDIA_TYPE_PLAIN, info);
        Request request = new Request.Builder()
                .url(url)
                .post(stringBody)
                .tag(tag)
                .build();

        LogUtil.d("请求接口名称：" + tag);
        LogUtil.json(info);
        return request;
    }

    /**
     * 无参数的网络请求
     *
     * @param url
     * @param tag
     * @return
     */
    public static Request getRequest(@NonNull String url, @NonNull String tag) {
        Request request = new Request.Builder()
                .url(url)
                .tag(tag)
                .build();

        LogUtil.d("请求接口名称: " + tag);
        return request;
    }

    /**
     * 执行网络请求
     *
     * @param request
     * @param callback
     */
    public static void enqueue(@NonNull Request request, @NonNull Callback callback) {
        Verify();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 取消某个请求
     *
     * @param tag
     */
    public static void cancelTag(Object tag) {
        Verify();
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    // 检测是否初始化了
    private static void Verify() {
        if (null == mParse) {
            throw new RuntimeException("请初始化OkhttpUtils！！！");
        }
    }

}
