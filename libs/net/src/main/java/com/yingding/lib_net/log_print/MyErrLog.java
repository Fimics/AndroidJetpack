package com.yingding.lib_net.log_print;

import android.util.Log;

/**
 * @author ytf
 * Created by on 2021/04/21 11:05
 * 使用错误打印级别
 */
public class MyErrLog implements Logger
{
    @Override
    public void log(int level, String tag, String msg)
    {
        Log.e(tag, msg);
    }
}
