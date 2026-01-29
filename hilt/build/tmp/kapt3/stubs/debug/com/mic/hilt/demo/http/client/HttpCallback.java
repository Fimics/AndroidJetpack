package com.mic.hilt.demo.http.client;

/**
 * 回调接口的一种实现  new HttpCallback<javaBean>
 * </javaBean>
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\b&\u0018\u0000*\u0004\b\u0000\u0010\u00012\u00020\u0002B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0014\u0010\u0004\u001a\u0006\u0012\u0002\b\u00030\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0002J\u0012\u0010\b\u001a\u00020\t2\b\u0010\n\u001a\u0004\u0018\u00010\u000bH\u0016J\u0015\u0010\f\u001a\u00020\t2\u0006\u0010\r\u001a\u00028\u0000H&\u00a2\u0006\u0002\u0010\u000eJ\u0012\u0010\f\u001a\u00020\t2\b\u0010\u000f\u001a\u0004\u0018\u00010\u000bH\u0016\u00a8\u0006\u0010"}, d2 = {"Lcom/mic/hilt/demo/http/client/HttpCallback;", "Result", "Lcom/mic/hilt/demo/http/client/ICallback;", "()V", "analysisClassInfo", "Ljava/lang/Class;", "object", "", "onFailure", "", "e", "", "onSuccess", "objResult", "(Ljava/lang/Object;)V", "result", "hilt_debug"})
public abstract class HttpCallback<Result extends java.lang.Object> implements com.mic.hilt.demo.http.client.ICallback {
    
    public HttpCallback() {
        super();
    }
    
    @java.lang.Override()
    public void onSuccess(@org.jetbrains.annotations.Nullable()
    java.lang.String result) {
    }
    
    public abstract void onSuccess(Result objResult);
    
    private final java.lang.Class<?> analysisClassInfo(java.lang.Object object) {
        return null;
    }
    
    @java.lang.Override()
    public void onFailure(@org.jetbrains.annotations.Nullable()
    java.lang.String e) {
    }
}