package com.mic.hilt.demo.http.client;

/**
 * 房产公司
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J6\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0018\u0010\u0006\u001a\u0014\u0012\u0006\u0012\u0004\u0018\u00010\u0005\u0012\u0006\u0012\u0004\u0018\u00010\u0001\u0018\u00010\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH&\u00a8\u0006\n"}, d2 = {"Lcom/mic/hilt/demo/http/client/IHttpProcessor;", "", "post", "", "url", "", "params", "", "callback", "Lcom/mic/hilt/demo/http/client/ICallback;", "hilt_debug"})
public abstract interface IHttpProcessor {
    
    public abstract void post(@org.jetbrains.annotations.Nullable()
    java.lang.String url, @org.jetbrains.annotations.Nullable()
    java.util.Map<java.lang.String, ? extends java.lang.Object> params, @org.jetbrains.annotations.Nullable()
    com.mic.hilt.demo.http.client.ICallback callback);
}