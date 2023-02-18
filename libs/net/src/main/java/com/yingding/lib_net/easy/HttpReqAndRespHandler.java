package com.yingding.lib_net.easy;

import java.util.Map;

/**
 * Created by Administrator on 2019/01/19.
 */
public abstract class HttpReqAndRespHandler
{
    public String handleBeforeReq(String src)
    {
        return src;
    }

    public void printLog(String msg){}

    public void resultErrHandle(final ApiException exp, final OkHttpResultCallback callback)
    {
        int erCode = exp.getCode();
        if (CommonCode.isNetErr(erCode))
        {
            callback.onNetErr(erCode, "网络连接出问题啦~");
        }else if(CommonCode.isServerErr(erCode))
        {
            callback.onError("server err", CommonCode.GLOBAL_SERVER_ERR, new Exception("server err"));
        }else if(CommonCode.isJsonParseErr(erCode))
        {
            callback.onError("resp err", CommonCode.GLOBAL_SERVER_ERR, new Exception("resp err"));
        }else
        {
            callback.onError(exp.getMessage(), erCode, exp);
        }
    }

    public void handleMultiParam(String method, Map<String, String> sourceParam)
    {
    }

    public void runOnMainThread(Runnable runnable)
    {
        runnable.run();
    }
}
