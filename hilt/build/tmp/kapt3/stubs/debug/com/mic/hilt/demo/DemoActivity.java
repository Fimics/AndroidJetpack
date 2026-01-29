package com.mic.hilt.demo;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0014R\u001a\u0010\u0003\u001a\u0004\u0018\u00010\u00048\u0006@\u0006X\u0087\u000e\u00a2\u0006\b\n\u0000\u0012\u0004\b\u0005\u0010\u0002\u00a8\u0006\n"}, d2 = {"Lcom/mic/hilt/demo/DemoActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "http", "Lcom/mic/hilt/demo/http/client/IHttpProcessor;", "getHttp$annotations", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "hilt_debug"})
public final class DemoActivity extends androidx.appcompat.app.AppCompatActivity {
    @javax.inject.Inject()
    @kotlin.jvm.JvmField()
    @org.jetbrains.annotations.Nullable()
    public com.mic.hilt.demo.http.client.IHttpProcessor http;
    
    public DemoActivity() {
        super();
    }
    
    @com.mic.hilt.demo.http.annoation.BindXUtils()
    @java.lang.Deprecated()
    public static void getHttp$annotations() {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
}