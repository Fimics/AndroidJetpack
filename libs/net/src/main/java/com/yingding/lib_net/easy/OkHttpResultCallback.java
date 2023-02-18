package com.yingding.lib_net.easy;

import com.google.gson.internal.$Gson$Types;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by ytf on 2019/04/30.
 * Description:
 */
public abstract class OkHttpResultCallback<T>
{
    Type mType;

    public OkHttpResultCallback()
    {
        mType = getSuperclassTypeParameter(getClass(), 0);
    }

    public static Type getSuperclassTypeParameter(Class<?> subclass, int type)
    {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class)
        {
            throw new RuntimeException("ResultCallback泛型 缺少类型");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[type]);
    }

    public abstract void onError(String simpleMsg, int code, Exception e);

    public void onNetErr(int code, String msg){}

    public void configHeader(Request.Builder builder){}

    /***
     * <b>外部如非必要不要重写该方法！</b>
     * 根据外层JSONObject获取服务端错误信息，用于显示给用户
     * @param obj
     * @return
     */
    public abstract String getServerFailedMessage(JSONObject obj) throws JSONException;

    /***
     * <b>外部如非必要不要重写该方法！</b>
     * 根据外层JSONObject获取服务端错误代码，用于对应作出处理
     * @param obj
     * @return
     */
    public abstract int getServerCode(JSONObject obj) throws JSONException;

    /***
     * 是否需要对返回结果做判断，只有返回码正确是才解析数据。
     * 默认是返回false，即服务器返回了数据就按传入的对象类型解析。建议加入对返回值得判断
     */
    public boolean needVerifyResultCode()
    {
        return true;
    }

    /***
     * 根据最外层的JSONObject判断返回码是否是正确的
     */
    public abstract boolean isSuccess(JSONObject rawData) throws JSONException;

    /***
     * 是否需要自定义返回
     * @return
     */
    public boolean needCustomReturn()
    {
        return false;
    }

    /***
     * 这里提供自定返回的class，不能提供泛型，框架默认使用BaseResp架构，这里提供的class只能是BaseResp<T>其中泛型T的类型，注意!
     * @return
     * @param serverCode 可以根据不同的code返回不同的类型class
     */
    public Class getCustomReturnClass(int serverCode)
    {
        return null;
    }

    /***
     * 自定义返回重写这个方法
     * @param serverCode 自定义返回码
     * @param d
     */
    public void onCustomSuccess(int serverCode, Object d)
    {

    }

    public boolean withCache()
    {
        return false;
    }

    public boolean isCacheTimeOut()
    {
        return false;
    }

    /***
     * 成功后回调
     * @param response
     */
    public abstract void onSuccess(T response);
}
