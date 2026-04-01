package com.noetix.libcore.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import androidx.arch.core.executor.ArchTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("RestrictedApi")
public class TaskExecutors {

    private final Handler mainHandler;
    private final ArchTaskExecutor archTaskExecutor;
    private final Executor ioExecutor;


    private TaskExecutors() {
        this.mainHandler = new Handler(Looper.getMainLooper());
        archTaskExecutor = ArchTaskExecutor.getInstance();
        this.ioExecutor = createIOExecutor();
    }

    private static class Holder {
        private static final TaskExecutors instance = new TaskExecutors();
    }

    public Handler getMainHandler(){
        return mainHandler;
    }
    public static TaskExecutors get(){
        return Holder.instance;
    }

    public void onIOTask(Runnable runnable) {
        ioExecutor.execute(runnable);
    }

    public void onMainTask(Runnable runnable) {
        archTaskExecutor.postToMainThread(runnable);
    }

    @SuppressWarnings("unused")
    public boolean isMainThread() {
        return  archTaskExecutor.isMainThread();
    }

    public void post(Runnable runnable){
        mainHandler.post(runnable);
    }

    public void postDelayed(Runnable runnable,long time){
        mainHandler.postDelayed(runnable,time);
    }

    public void removeCallback(Runnable runnable) {
        mainHandler.removeCallbacks(runnable);
    }

    private Executor createIOExecutor() {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(numberOfCores, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("nx_thread_io_" + threadNumber.getAndIncrement());
                return t;
            }
        });
    }

}
