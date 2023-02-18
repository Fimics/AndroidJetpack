package com.yingding.lib_net.easy;

/**
 * Created by yutf on 2017/8/23 0023.
 */

public class ApiException extends RuntimeException
{
    private int code;

    public ApiException(int code, String msg)
    {
        super(msg);
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
