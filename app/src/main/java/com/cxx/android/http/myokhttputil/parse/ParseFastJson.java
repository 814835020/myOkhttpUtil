package com.cxx.android.http.myokhttputil.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cxx.android.http.myokhttputil.util.LogUtil;

import java.util.List;
import java.util.Map;


/**
 * Created by cxx on 16/5/11.
 * 使用FastJson解析
 */
public class ParseFastJson implements Parse {

    @Override
    public <T> T parseReuslt(String resultInfo, Class<T> clazz) {

        T obj = null;
        try {
            obj = JSON.parseObject(resultInfo, clazz);
        } catch (Exception e) {
            LogUtil.d("JSON解析异常： " + resultInfo);
        }
        return obj;
    }

    @Override
    public <T> Map<String, T> parseDictionary (String resultInfo) {

        Map<String, T> map = null;
        try {
            map = JSON.parseObject(resultInfo, new TypeReference<Map<String, T>>() {
            });
        } catch (Exception e) {
            LogUtil.d("JSON解析异常： " + resultInfo);
        }
        return map;
    }

    @Override
    public <T> List<T> parseArray(String resultInfo, Class<T> clazz) {

        List<T> obj = null;
        try {
            obj = JSON.parseArray(resultInfo, clazz);
        } catch (Exception e) {
            LogUtil.d("JSON解析异常： " + resultInfo);
        }
        return obj;
    }
}
