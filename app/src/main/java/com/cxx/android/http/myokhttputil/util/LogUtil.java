package com.cxx.android.http.myokhttputil.util;

import com.cxx.android.http.myokhttputil.Log.L;
import com.cxx.android.http.myokhttputil.Log.LogInterface;

/**
 * Created by xuan on 2017/3/28.
 */

public class LogUtil {

    private static L l = new L();

    public static void isDebug(boolean isDebug) {
        l.isDebug(isDebug);
    }

    public static void setLog(LogInterface log) {
        l.setLog(log);
    }

    public static void v(String info) {
        l.v(info);
    }

    public static void i(String info) {
        l.i(info);
    }

    public static void d(String info) {
        l.d(info);
    }

    public static void e(String info) {
        l.e(info);
    }

    public static void json(String info) {
        l.json(info);
    }
}
