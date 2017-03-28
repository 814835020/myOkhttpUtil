package com.cxx.android.http.myokhttputil.Log;

/**
 * Created by idea on 16/5/12.
 */
public class MyLog implements LogInterface {

    @Override
    public void v(String info) {
        android.util.Log.v("myOkhttpUtil", info);
    }

    @Override
    public void i(String info) {
        android.util.Log.i("myOkhttpUtil", info);
    }

    @Override
    public void d(String info) {
        android.util.Log.d("myOkhttpUtil", info);
    }

    @Override
    public void e(String info) {
        android.util.Log.e("myOkhttpUtil", info);
    }

    @Override
    public void json(String info) {
        android.util.Log.i("myOkhttpUtil", info);
    }

}
