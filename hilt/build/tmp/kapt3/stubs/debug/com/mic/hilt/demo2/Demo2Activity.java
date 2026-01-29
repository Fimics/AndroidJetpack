package com.mic.hilt.demo2;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001e\u0010\u000b\u001a\u00020\f8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010\u00a8\u0006\u0015"}, d2 = {"Lcom/mic/hilt/demo2/Demo2Activity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "TAG", "", "student", "Lcom/mic/hilt/demo2/Student;", "getStudent", "()Lcom/mic/hilt/demo2/Student;", "setStudent", "(Lcom/mic/hilt/demo2/Student;)V", "user", "Lcom/mic/hilt/demo2/User;", "getUser", "()Lcom/mic/hilt/demo2/User;", "setUser", "(Lcom/mic/hilt/demo2/User;)V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "hilt_debug"})
public final class Demo2Activity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "Demo2Activity";
    @javax.inject.Inject()
    public com.mic.hilt.demo2.User user;
    @javax.inject.Inject()
    public com.mic.hilt.demo2.Student student;
    
    public Demo2Activity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.mic.hilt.demo2.User getUser() {
        return null;
    }
    
    public final void setUser(@org.jetbrains.annotations.NotNull()
    com.mic.hilt.demo2.User p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.mic.hilt.demo2.Student getStudent() {
        return null;
    }
    
    public final void setStudent(@org.jetbrains.annotations.NotNull()
    com.mic.hilt.demo2.Student p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
}