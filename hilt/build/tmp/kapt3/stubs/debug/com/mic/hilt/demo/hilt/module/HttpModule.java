package com.mic.hilt.demo.hilt.module;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007\u00a8\u0006\u0005"}, d2 = {"Lcom/mic/hilt/demo/hilt/module/HttpModule;", "", "()V", "getHttpObject", "Lcom/mic/hilt/demo/hilt/object/HttpObject;", "hilt_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.android.components.ActivityComponent.class})
public final class HttpModule {
    
    public HttpModule() {
        super();
    }
    
    @dagger.Provides()
    @dagger.hilt.android.scopes.ActivityScoped()
    @org.jetbrains.annotations.NotNull()
    public final com.mic.hilt.demo.hilt.object.HttpObject getHttpObject() {
        return null;
    }
}