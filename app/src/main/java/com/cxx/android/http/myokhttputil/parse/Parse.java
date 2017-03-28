package com.cxx.android.http.myokhttputil.parse;

import java.util.List;
import java.util.Map;

/**
 * Created by cxx on 16/5/10.
 * <p/>
 * 解析接口类，方便注入操作，可一定义解析方案
 */
public interface Parse {

    // 对象的方式解析返回的字符串
    <T> T parseReuslt(String resultInfo, Class<T> clazz);

    // 字典的方式解析返回的数据
    <T> Map<String, T> parseDictionary(String resultInfo);

    // 数组的方式解析返回的数据
    <T> List<T> parseArray(String resultInfo, Class<T> clazz);
}
