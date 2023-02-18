package com.yingding.lib_net.log_print;


import okhttp3.internal.platform.Platform;

@SuppressWarnings({"WeakerAccess", "unused"})
public interface Logger
{
    Logger DEFAULT = new Logger()
    {
        @Override
        public void log(int level, String tag, String message)
        {
//            Platform.get().log(message, level,  null);
            Platform.get().log(level,  message, null);
        }
    };

    void log(int level, String tag, String msg);
}