package com.mic.rtmp;

import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    private DerryPusher pusher; // 中转站（C++层打交道） 视频 和 音频

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        /*cameraHelper = new CameraHelper(this, Camera.CameraInfo.CAMERA_FACING_FRONT, 640, 480);
        cameraHelper.setPreviewDisplay(surfaceView.getHolder());*/

        // 前置摄像头，宽，高，fps(每秒25帧)，码率/比特率：https://blog.51cto.com/u_7335580/2058648
        pusher = new DerryPusher(this, Camera.CameraInfo.CAMERA_FACING_FRONT, 640, 480, 25, 800000);
        pusher.setPreviewDisplay(surfaceView.getHolder());
    }


    /**
     * 切换摄像头
     * @param view
     */
    public void switchCamera(View view) {
        pusher.switchCamera();
    }

    /**
     * 开始直播
     * @param view
     */
    public void startLive(View view) {
        // pusher.startLive("rtmp://139.224.136.101/myapp");

        pusher.startLive("rtmp://sendtc3a.douyu.com/live/9835435rvBddb3av?wsSecret=71a52cc241fe730a2e10d27f1c9c0899&wsTime=60c2205f&wsSeek=off&wm=0&tw=0&roirecognition=0&record=flv&origin=tct");

        // 视频 音频 所有的代码，按标准来的， ffplay
        // 斗鱼的音频怎么处理的还不知道
    }

    /**
     * 停止直播
     * @param view
     */
    public void stopLive(View view) {
        pusher.stopLive();
    }

    /**
     * 释放工作
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        pusher.release();
    }

    // OpenCV OpenGC 高深
    // WebRTC 直接视频预览
    // 串口(智能家居的子集)

    // Fluuter

    // 雷霄华CSDN：SDL 视频：
    /*链接：https://pan.baidu.com/s/1d46l3sJGbrv3jKDcdN_D_w
    提取码：pgd5
    复制这段内容后打开百度网盘手机App，操作更方便哦--来自百度网盘超级会员V5的分享*/
}
