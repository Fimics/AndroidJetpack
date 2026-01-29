package com.mic.hilt.demo.http.client;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\"\u0010\u0007\u001a\u00020\b2\u0018\u0010\t\u001a\u0014\u0012\u0006\u0012\u0004\u0018\u00010\u000b\u0012\u0006\u0012\u0004\u0018\u00010\f\u0018\u00010\nH\u0002J6\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u000b2\u0018\u0010\t\u001a\u0014\u0012\u0006\u0012\u0004\u0018\u00010\u000b\u0012\u0006\u0012\u0004\u0018\u00010\f\u0018\u00010\n2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/mic/hilt/demo/http/client/OkHttpProcessor;", "Lcom/mic/hilt/demo/http/client/IHttpProcessor;", "()V", "mOkHttpClient", "Lokhttp3/OkHttpClient;", "myHandler", "Landroid/os/Handler;", "appendBody", "Lokhttp3/RequestBody;", "params", "", "", "", "post", "", "url", "callback", "Lcom/mic/hilt/demo/http/client/ICallback;", "hilt_debug"})
public final class OkHttpProcessor implements com.mic.hilt.demo.http.client.IHttpProcessor {
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient mOkHttpClient = null;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler myHandler = null;
    
    @javax.inject.Inject()
    public OkHttpProcessor() {
        super();
    }
    
    @java.lang.Override()
    public void post(@org.jetbrains.annotations.Nullable()
    java.lang.String url, @org.jetbrains.annotations.Nullable()
    java.util.Map<java.lang.String, ? extends java.lang.Object> params, @org.jetbrains.annotations.Nullable()
    com.mic.hilt.demo.http.client.ICallback callback) {
    }
    
    private final okhttp3.RequestBody appendBody(java.util.Map<java.lang.String, ? extends java.lang.Object> params) {
        return null;
    }
}