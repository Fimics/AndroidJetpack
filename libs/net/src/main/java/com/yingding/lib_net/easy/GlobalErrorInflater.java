package com.yingding.lib_net.easy;

import org.json.JSONException;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by yutf on 2017/8/23 0023.
 */

public class GlobalErrorInflater
{

    public static ApiException getError(Throwable e)
    {
        System.out.println("异常类信息: " + e.toString());
        ApiException ex;
        if (e instanceof ConnectException)
        {
            ex = new ApiException(CommonCode.HTTP_CONNECTION_ERROR, CommonCode.HTTP_CONNECTION_ERROR_MSG);
        }else if(e instanceof UnknownHostException)
        {
            ex = new ApiException(CommonCode.HTTP_UNKNOWN_HOST_ERROR, CommonCode.HTTP_UNKNOWN_HOST_ERROR_MSG);
        }else if(e instanceof SocketTimeoutException)
        {
            ex = new ApiException(CommonCode.HTTP_TIMEOUT_ERROR, CommonCode.HTTP_TIMEOUT_ERROR_MSG);
        }else if (e instanceof com.google.gson.JsonParseException ||
                e instanceof JSONException)
        {
            ex = new ApiException(CommonCode.INNER_JSON_PARSE_ERROR, CommonCode.INNER_JSON_PARSE_ERROR_MSG);
        } else if (e instanceof FileNotFoundException)
        {
            ex = new ApiException(CommonCode.INNER_FILE_NOT_FOUND_ERROR, CommonCode.INNER_ERROR_MSG);
        } else if (e instanceof IllegalArgumentException)
        {
            ex = new ApiException(CommonCode.INNER_FILE_NOT_FOUND_ERROR, CommonCode.INNER_ERROR_MSG);
        }else
        {
            if(e instanceof ApiException)
            {
                ex = (ApiException) e;
            }else
            {
                ex = new ApiException(CommonCode.UNKNOWN_ERROR, CommonCode.UNKNOWN_ERROR_MSG);
            }
        }
        return ex;
    }
}
