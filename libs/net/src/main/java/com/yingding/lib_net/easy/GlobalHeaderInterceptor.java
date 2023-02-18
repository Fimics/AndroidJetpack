package com.yingding.lib_net.easy;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.Map;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ytf on 2019/08/08.
 * Description:
 */
public class GlobalHeaderInterceptor implements Interceptor
{

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException
    {
        Request original = chain.request();
        Map<String, String> header = provideHeaders();
        if(header != null && header.size() > 0)
        {
            Request.Builder builder = original.newBuilder();
            for (String k: header.keySet())
            {
                String v = header.get(k);

                if(!TextUtils.isEmpty(v))
                {
                    builder.header(k, v);
                }
            }
            return chain.proceed(builder.build());
        }
        return chain.proceed(original);
    }

    public Map<String, String> provideHeaders()
    {
        return null;
    }
}
