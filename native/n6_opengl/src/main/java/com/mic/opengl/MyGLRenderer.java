package com.mic.opengl;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.util.Log;
import com.mic.opengl.face.FaceTrack;
// import com.derry.opengl.filter.BeautyFilter;
import com.mic.opengl.filter.BeautyFilter;
import com.mic.opengl.filter.BigEyeFilter;
import com.mic.opengl.filter.BigEyeFilter22222;
import com.mic.opengl.filter.CameraFilter;
import com.mic.opengl.filter.ScreenFilter;
import com.mic.opengl.filter.StickFilter;
import com.mic.opengl.record.MyMediaRecorder;
import com.mic.opengl.utils.CameraHelper;
import com.mic.opengl.utils.FileUtil;
import java.io.IOException;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static android.opengl.GLES20.*;

public class MyGLRenderer implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {

    private MyGLSurfaceView mGLSurfaceView;
    private CameraHelper mCameraHelper;
    private int[] mTextureID;
    private SurfaceTexture mSurfaceTexture;
    private ScreenFilter mScreenFilter;
    private CameraFilter mCameraFilter;
    private float[] mtx = new float[16];
    private MyMediaRecorder mMediaRecorder;
    private int mWidth;
    private int mHeight;

    private BigEyeFilter22222 mBigEyeFilter; // TODO 【大眼相关代码】
    private FaceTrack mFaceTrack; // TODO 【大眼相关代码】
    private StickFilter mStickFilter; // TODO 【贴纸相关代码】
    private BeautyFilter mBeautyFilter; // TODO 【美颜相关代码】

    public MyGLRenderer(MyGLSurfaceView myGLSurfaceView) {
        mGLSurfaceView = myGLSurfaceView;
        // TODO 【大眼相关代码】  assets Copy到SD卡
        FileUtil.copyAssets2SDCard(mGLSurfaceView.getContext(), "lbpcascade_frontalface.xml",
                "/sdcard/lbpcascade_frontalface.xml"); // OpenCV的模型
        FileUtil.copyAssets2SDCard(mGLSurfaceView.getContext(), "seeta_fa_v1.1.bin",
                "/sdcard/seeta_fa_v1.1.bin"); // 中科院的模型
    }

    /**
     * Surface 创建时 回调
     * @param gl     1.0 api遗留参数
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraHelper = new CameraHelper((Activity) mGLSurfaceView.getContext(),
                Camera.CameraInfo.CAMERA_FACING_FRONT, 800, 480);
        mCameraHelper.setPreviewCallback(this);
        // 准备摄像头绘制的画布
        mTextureID = new int[1];
        glGenTextures(mTextureID.length, mTextureID, 0);
        mSurfaceTexture = new SurfaceTexture(mTextureID[0]);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mCameraFilter = new CameraFilter(mGLSurfaceView.getContext());
        mScreenFilter = new ScreenFilter(mGLSurfaceView.getContext());

        EGLContext eglContext = EGL14.eglGetCurrentContext();

        mMediaRecorder = new MyMediaRecorder(480, 800,
                "/sdcard/test_" + System.currentTimeMillis() + ".mp4", eglContext,
                mGLSurfaceView.getContext());
    }

    /**
     * Surface 发生改变回调
     * @param gl
     * @param width  720   OpenGL相关的
     * @param height 1022  OpenGL相关的
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        // 创建人脸检测跟踪器 // TODO 【大眼相关的人脸追踪/人脸关键点的代码】
        mFaceTrack = new FaceTrack("/sdcard/lbpcascade_frontalface.xml","/sdcard/seeta_fa_v1.1.bin", mCameraHelper);
        mFaceTrack.startTrack(); // 启动跟踪器

        mCameraHelper.startPreview(mSurfaceTexture);

        mCameraFilter.onReady(width, height);
        mScreenFilter.onReady(width, height);
    }

    /**
     * 绘制一帧图像时 回调
     * 注意：该方法中一定要进行绘制操作
     * 该方法返回后，会交换渲染缓冲区，如果不绘制任何东西，会导致屏幕闪烁
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        glClearColor(255, 0, 0, 0);//屏幕清理颜色 红色
        // mask
        // GL_COLOR_BUFFER_BIT 颜色缓冲区
        // GL_DEPTH_BUFFER_BIT 深度
        // GL_STENCIL_BUFFER_BIT 模型
        glClear(GL_COLOR_BUFFER_BIT);

        // 绘制摄像头数据
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mtx);

        mCameraFilter.setMatrix(mtx);
        // mCameraFilter： 摄像头数据先输出到 fbo
        int textureId = mCameraFilter.onDrawFrame(mTextureID[0]);

        // TODO 【大眼相关代码】
        // TODO textureId = 大眼Filter.onDrawFrame(textureId);
        if (null != mBigEyeFilter) {
            mBigEyeFilter.setFace(mFaceTrack.getFace());
            textureId = mBigEyeFilter.onDrawFrame(textureId);
        }

        // TODO 【贴纸相关代码】
        if (null != mStickFilter) {
            mStickFilter.setFace(mFaceTrack.getFace()); // 需要定位人脸，所以需要 JavaBean
            textureId = mStickFilter.onDrawFrame(textureId);
        }

        // TODO 【美颜相关代码】
        if (null != mBeautyFilter) { // 没有不需要 人脸追踪/人脸关键点，整个屏幕美颜
            textureId = mBeautyFilter.onDrawFrame(textureId);
        }

        // textureId = xxxFilter.onDrawFrame(textureId);
        // ... textureId == 大眼后的纹理ID
        mScreenFilter.onDrawFrame(textureId);

        // 录制
        mMediaRecorder.encodeFrame(textureId, mSurfaceTexture.getTimestamp());
    }

    /**
     * surfaceTexture 画布有一个有效的新图像时 回调
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLSurfaceView.requestRender();
    }

    public void surfaceDestroyed() {
        mCameraHelper.stopPreview();
        mFaceTrack.stopTrack(); // 停止跟踪器
    }

    /**
     * 开始录制
     * @param speed
     */
    public void startRecording(float speed) {
        Log.e("MyGLRender", "startRecording");
        try {
            mMediaRecorder.start(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        Log.e("MyGLRender", "stopRecording");
        mMediaRecorder.stop();
    }

    /** // TODO 【大眼相关代码】
     * 开启大眼特效
     * @param isChecked
     */
    public void enableBigEye(final boolean isChecked) {
        // BigEyeFilter bigEyeFilter = new BigEyeFilter(); // 这样可以吗  不行，必须在EGL线程里面绘制

        mGLSurfaceView.queueEvent(new Runnable() { // 把大眼渲染代码，加入到， GLSurfaceView 的 内置EGL 的 GLTHread里面
            public void run() {
                if (isChecked) {
                    mBigEyeFilter = new BigEyeFilter22222(mGLSurfaceView.getContext());
                    mBigEyeFilter.onReady(mWidth, mHeight);
                } else {
                    mBigEyeFilter.release();
                    mBigEyeFilter = null;
                }
            }
        });
    }

    // Camera画面只有有数据，就会回调此函数
    @Override // 要把相机的数据，给C++层做人脸追踪  // TODO 【大眼相关代码】
    public void onPreviewFrame(byte[] data, Camera camera) {
        mFaceTrack.detector(data);
    }

    // TODO 下面是 NDK OpenGL 53节课新增点
    /**
     * TODO 开启贴纸
     * @param isChecked checkbox复选框是否勾上了
     */
    public void enableStick(final boolean isChecked) {
        mGLSurfaceView.queueEvent(new Runnable() { // 在EGL线程里面绘制 贴纸工作
            public void run() {
                if (isChecked) {
                    mStickFilter = new StickFilter(mGLSurfaceView.getContext());
                    mStickFilter.onReady(mWidth, mHeight);
                } else {
                    mStickFilter.release();
                    mStickFilter = null;
                }
            }
        });
    }

    /**
     * TODO 开启美颜
     * @param isChecked checkbox复选框是否勾上了
     */
    public void enableBeauty(final boolean isChecked) {
        mGLSurfaceView.queueEvent(new Runnable() {
            public void run() {
                if (isChecked) {
                    mBeautyFilter = new BeautyFilter(mGLSurfaceView.getContext());
                    mBeautyFilter.onReady(mWidth, mHeight);
                } else {
                    mBeautyFilter.release();
                    mBeautyFilter = null;
                }
            }
        });
    }
}
