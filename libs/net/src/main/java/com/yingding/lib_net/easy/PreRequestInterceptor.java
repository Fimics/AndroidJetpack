package com.yingding.lib_net.easy;

import org.jetbrains.annotations.NotNull;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by ytf on 2021/4/11 011.
 * Description:
 */
public abstract class PreRequestInterceptor implements Interceptor {


    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        if(!canRequest()){
            chain.call().cancel();
            return null;
        }else{
            return chain.proceed(chain.request());
        }
    }

    public abstract boolean canRequest();
}
