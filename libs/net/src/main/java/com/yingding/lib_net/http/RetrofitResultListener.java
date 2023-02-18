package com.yingding.lib_net.http;

/**网络返回结果*/
public interface RetrofitResultListener<T> {
     void onResult(T t);
}
