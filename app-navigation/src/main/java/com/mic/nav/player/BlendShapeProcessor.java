package com.mic.nav.player;

import com.noetix.libnoetix.Callback;
import com.noetix.libnoetix.FramePair;
import com.noetix.libnoetix.IRobotSDKManager;
import com.noetix.libnoetix.TTSHelper;
import com.noetix.libnoetix.entity.AudioFrame;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlendShapeProcessor implements Callback {
    private static final String TAG ="BlendShapeProcessor_Test";
    private ExecutorService executorService;
    // 使用指数移动平均滤波 + 速度限制来消除抖动
    //private NeckMotionSmoother neckSmoother; // 添加平滑器
//    smoothingFactor	0.2-0.4	值越小越平滑但响应越慢，0.3是平衡值
//    maxDeltaPerFrame	0.03-0.08	限制突变幅度，根据帧率调整，30fps建议0.05

    private AntiJitterNeckSmoother neckSmoother; // 替换原来的平滑器



    public BlendShapeProcessor() {
        executorService = Executors.newScheduledThreadPool(3);
        // 初始化平滑器，参数可根据实际调整
//        neckSmoother = new NeckMotionSmoother(0.1f, 0.01f);
        neckSmoother = new AntiJitterNeckSmoother(); // 使用抗抖动版本
    }

    @Override
    public void onOriginResult(float[] outputParams, float[] audioData) {

    }

    @Override
    public void onSynchronousResult(FramePair framePair, Map<String, Float> csvBlendShapes, LinkedList<FramePair> pcmList) {


        if (framePair == null) {
//            KLog.d(TAG,"this ->"+this.toString());
//

//            KLog.d(TAG, "当前无数据可处理  framePair==null");
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    sendMotorCommand(csvBlendShapes);
                }
            });
        } else {
            //handle tts
//            float[] floatsAudio =framePair.getAudioFrame();
//            int id = framePair.getIndex();
//            byte [] audioData = TTSHelper.floatArrayToByte(floatsAudio, ByteOrder.LITTLE_ENDIAN);
//            AudioFrame audioFrame = new AudioFrame(id,audioData);
//            nxPlayer.enqueueFrame(audioFrame);


            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Iterator<FramePair> iterator = pcmList.iterator();
                    while (iterator.hasNext()) {
                        FramePair framePairPcm = iterator.next();
                        float[] floatsAudio = framePairPcm.getAudioFrame();
                        int id = framePairPcm.getIndex();
                        byte[] audioData = TTSHelper.floatArrayToByte(floatsAudio, ByteOrder.LITTLE_ENDIAN);
                        AudioFrame audioFrame = new AudioFrame(id, audioData);
                        NXPlayer.getInstance().enqueueFrame(audioFrame);
                        iterator.remove();
                    }

                    Map<String, Float> blendShapes = framePair.getBlendShapeFrame();
                    float JawOpen = blendShapes.get("JawOpen");

//                    KLog.d(TAG, "blendShapes JawOpen   " + JawOpen);
//                    if (Math.abs(JawOpen)>1){
//                        KLog.d(TAG, "blendShapes JawOpen-------------------------   " + JawOpen);
//                    }

                    float[] motorCommands = bs2MotorCommands(blendShapes);
                    float[] csvCommands = bs2MotorCommands(csvBlendShapes);
                    float[] mergedCommands = createUpdatedBlendShapes(csvCommands, motorCommands);
//                    KLog.d(TAG," IRobotSDKManager.getInstance().setFaceAngles(mergedCommands)");
                    IRobotSDKManager.getInstance().setFaceAngles(mergedCommands);
                }
            });
        }
    }


    private void sendMotorCommand(Map<String, Float> blendShapes) {
//        float JawOpen =blendShapes.get("JawOpen");
//        KLog.d(TAG,"JawOpen "+JawOpen);
        if (blendShapes == null || blendShapes.isEmpty()) {
//            KLog.d(TAG, "当前无数据可处理");
            return;
        }
        try {
            float[] motorCommands = bs2MotorCommands(blendShapes);
            IRobotSDKManager.getInstance().setFaceAngles(motorCommands);
        } catch (Exception e) {
//            KLog.e(TAG, "执行电机指令时发生异常", e);
        }
    }

    private float[] bs2MotorCommands(Map<String, Float> blendShapes) {
        int[] motorAngles = IRobotSDKManager.getInstance().mappingMotor(blendShapes);
        float[] motorCommands = new float[motorAngles.length];
        for (int i = 0; i < motorAngles.length; i++) {
            motorCommands[i] = motorAngles[i];
        }
        return motorCommands;
    }

    public float[] createUpdatedBlendShapes(float[] csvMotorCommands, float[] motorCommands) {
        // 确定新数组的长度为两个输入数组的较大值
        int newLength = csvMotorCommands.length;
        float[] updated = Arrays.copyOf(motorCommands, newLength);

        for (int i = 0; i < newLength; i++) {
            if (i <= 14) {
                updated[i] = csvMotorCommands[i];
            } else {
                updated[i] = motorCommands[i];
            }
        }
        return updated;
    }

    private float [] getNecksArray(Map<String, Float> blendShape){

        // 安全获取值并处理null情况
        Float pitchVal = blendShape.get("HeadPitch");
        Float rollVal = blendShape.get("HeadRoll");
        Float yawVal = blendShape.get("HeadYaw");

        // 使用三元运算符处理可能的null值
        float pitch = (pitchVal != null) ? pitchVal : 0.0f;
        float roll = (rollVal != null) ? -rollVal : 0.0f; // 注意负号处理
        float yaw = (yawVal != null) ? yawVal : 0.0f;

        float[] neckArray = new float[3];
        neckArray[0] = pitch / 0.5f;
        neckArray[1] = roll / 0.8f;
        neckArray[2] = yaw / 0.8f;
        return neckArray;
    }
}
