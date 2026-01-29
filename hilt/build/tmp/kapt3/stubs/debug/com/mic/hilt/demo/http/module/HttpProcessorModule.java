package com.mic.hilt.demo.http.module;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\u0003\u001a\u0004\u0018\u00010\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\'J\u0014\u0010\u0007\u001a\u0004\u0018\u00010\u00042\b\u0010\b\u001a\u0004\u0018\u00010\tH\'J\u0014\u0010\n\u001a\u0004\u0018\u00010\u00042\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\'\u00a8\u0006\r"}, d2 = {"Lcom/mic/hilt/demo/http/module/HttpProcessorModule;", "", "()V", "bindOkhttp", "Lcom/mic/hilt/demo/http/client/IHttpProcessor;", "okHttpProcessor", "Lcom/mic/hilt/demo/http/client/OkHttpProcessor;", "bindVolley", "volleyProcessor", "Lcom/mic/hilt/demo/http/client/VolleyProcessor;", "bindXUtils", "xUtilsProcessor", "Lcom/mic/hilt/demo/http/client/XUtilsProcessor;", "hilt_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.android.components.ActivityComponent.class})
public abstract class HttpProcessorModule {
    
    public HttpProcessorModule() {
        super();
    }
    
    @com.mic.hilt.demo.http.annoation.BindOkhttp()
    @dagger.Binds()
    @org.jetbrains.annotations.Nullable()
    public abstract com.mic.hilt.demo.http.client.IHttpProcessor bindOkhttp(@org.jetbrains.annotations.Nullable()
    com.mic.hilt.demo.http.client.OkHttpProcessor okHttpProcessor);
    
    @com.mic.hilt.demo.http.annoation.BindVolley()
    @dagger.Binds()
    @org.jetbrains.annotations.Nullable()
    public abstract com.mic.hilt.demo.http.client.IHttpProcessor bindVolley(@org.jetbrains.annotations.Nullable()
    com.mic.hilt.demo.http.client.VolleyProcessor volleyProcessor);
    
    @com.mic.hilt.demo.http.annoation.BindXUtils()
    @dagger.Binds()
    @org.jetbrains.annotations.Nullable()
    public abstract com.mic.hilt.demo.http.client.IHttpProcessor bindXUtils(@org.jetbrains.annotations.Nullable()
    com.mic.hilt.demo.http.client.XUtilsProcessor xUtilsProcessor);
}