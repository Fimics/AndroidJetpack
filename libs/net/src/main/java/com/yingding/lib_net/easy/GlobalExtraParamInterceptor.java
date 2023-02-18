package com.yingding.lib_net.easy;


import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ytf on 2019/08/08.
 * Description:
 */
public class GlobalExtraParamInterceptor implements Interceptor
{

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException
    {
        Request original = chain.request();

        String method = original.method();

        Map<String, String> param = new HashMap<>();
        if ("GET".equals(method))
        {
            HttpUrl originalHttpUrl = original.url();
            int qsize = originalHttpUrl.querySize();
            if (qsize > 0)
            {
                for (int i = 0; i < qsize; i++)
                {
                    String key = originalHttpUrl.queryParameterName(i);
                    String value = originalHttpUrl.queryParameterValue(i);
                    param.put(key, value);
                }
            }
            handle(method, param);
            return chain.proceed(newGetRequest(original, originalHttpUrl, param));
        } else if("POST".equals(method))
        {
            RequestBody requestBody = original.body();

            if (requestBody != null)
            {
                //multiformbody不在这里处理
                if (requestBody instanceof FormBody)
                {
                    FormBody body = (FormBody) requestBody;
                    for (int i = 0; i < body.size(); i++)
                    {
                        String k = body.name(i);
                        String v = body.value(i);
                        param.put(k, v);
                    }
                    handle(method, param);
                    return chain.proceed(newPostRequest(original, param));
                }
            }
        }
        return chain.proceed(original);
    }

    private Request newGetRequest(Request original, HttpUrl originalHttpUrl, Map<String, String> params)
    {
        HttpUrl.Builder builder = originalHttpUrl.newBuilder();
        for (String k : params.keySet())
        {
            //先删除已经存在的，不然会重复
            builder.removeAllQueryParameters(k);
            String v = params.get(k);
            if (!TextUtils.isEmpty(v))
            {
                builder.addQueryParameter(k, v);
            }
        }
        return original.newBuilder()
                .url(builder.build())
                .build();
    }

    private Request newPostRequest(Request original, Map<String, String> params)
    {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (String k : params.keySet())
        {
            String value = params.get(k);
            if (!TextUtils.isEmpty(value))
            {
                formBodyBuilder.add(k, value);
            }
        }
        HttpUrl originalHttpUrl = original.url();

        return original.newBuilder()
                .url(originalHttpUrl.url())
                .post(formBodyBuilder.build())
                .build();
    }

    /**
     * 添加额外参数，如参数签名等
     *
     *
     * @param method
     * @param sourceParam
     * @return
     */
    public void handle(String method, Map<String, String> sourceParam)
    {
    }
}
