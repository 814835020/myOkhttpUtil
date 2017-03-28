package com.cxx.android.http.myokhttputil.Log;

/**
 * Created by xuan on 2017/3/27.
 */

public class L {

    private static LogInterface l = new MyLog();
    private static boolean isDebug = true;

    public void isDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void setLog(LogInterface l) {
        this.l = l;
    }

    public void v(String info) {
        if (isDebug)
            l.v(info);
    }

    public void i(String info) {
        if (isDebug)
            l.i(info);
    }

    public void d(String info) {
        if (isDebug)
            l.d(info);
    }

    public void e(String info) {
        if (isDebug)
            l.e(info);
    }

    public void json(String info) {
        if (isDebug)
            l.json(info);
    }
}
