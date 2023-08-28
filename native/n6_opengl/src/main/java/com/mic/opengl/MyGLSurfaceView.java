package com.mic.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class MyGLSurfaceView extends GLSurfaceView {
    private MyGLRenderer mRenderer;

    private Speed mSpeed = Speed.MODE_NORMAL;



    public enum Speed {
        MODE_EXTRA_SLOW, MODE_SLOW, MODE_NORMAL, MODE_FAST, MODE_EXTRA_FAST
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化
     */
    private void init(){
        setEGLContextClientVersion(2);//设置 EGL 版本
        mRenderer = new MyGLRenderer(this);
        setRenderer(mRenderer);//设置渲染器
//        setRenderer(new MyGLRenderer(this));//设置渲染器
        setRenderMode(RENDERMODE_WHEN_DIRTY);//设置按需渲染模式
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        mRenderer.surfaceDestroyed();
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        float speed = 1.0f;
        switch (mSpeed){
            case MODE_EXTRA_SLOW:
                speed = 0.3f;
                break;
            case MODE_SLOW:
                speed = 0.5f;
                break;
            case MODE_NORMAL:
                speed = 1.0f;
                break;
            case MODE_FAST:
                speed = 1.5f;
                break;
            case MODE_EXTRA_FAST:
                speed = 3.0f;
                break;

        }
        mRenderer.startRecording(speed);
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        mRenderer.stopRecording();
    }

    public void setSpeed(Speed speed) {
        mSpeed = speed;
    }

    /**
     * 开启大眼特效
     * @param isChecked
     */
    public void enableBigEye(boolean isChecked) {
        mRenderer.enableBigEye(isChecked);
    }

    // TODO 下面是 NDK OpenGL 53节课新增点
    /**
     * TODO 开启贴纸
     * @param isChecked
     */
    public void enableStick(boolean isChecked) {
        mRenderer.enableStick(isChecked);
    }

    /**
     * TODO 开启美颜
     * @param isChecked
     */
    public void enableBeauty(boolean isChecked) {
        mRenderer.enableBeauty(isChecked);
    }
}
