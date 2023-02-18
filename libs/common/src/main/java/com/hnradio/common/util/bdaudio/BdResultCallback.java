package com.hnradio.common.util.bdaudio;

import com.yingding.lib_net.easy.OkHttpResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

/***
 * 回调类，用于返回结果到前台
 *会自动根据类型产生对象，在post中用处较多
 */
public abstract class BdResultCallback<T> extends OkHttpResultCallback<T>
{

    /***
     * <b>外部如非必要不要重写该方法！</b>
     * 根据外层JSONObject获取服务端错误信息，用于显示给用户
     * @param obj
     * @return
     */
    public String getServerFailedMessage(JSONObject obj) throws JSONException
    {
        return "";
    }

    /***
     * <b>外部如非必要不要重写该方法！</b>
     * 根据外层JSONObject获取服务端错误代码，用于对应作出处理
     * @param obj
     * @return
     */
    public int getServerCode(JSONObject obj) throws JSONException
    {
        return 0;
    }

    /***
     * 根据最外层的JSONObject判断返回码是否是正确的
     */
    public boolean isSuccess(JSONObject rawData) throws JSONException
    {
        return true;
    }
}
