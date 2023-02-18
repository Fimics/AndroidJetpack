//package com.hnradio.common.util.bdasr;
//
//import android.content.Context;
//import android.os.Handler;
//import android.os.Looper;
//
//import com.baidu.speech.EventListener;
//import com.baidu.speech.EventManager;
//import com.baidu.speech.EventManagerFactory;
//import com.baidu.speech.asr.SpeechConstant;
//import com.hnradio.common.util.L;
//
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * Created by ytf on 2020/10/21 021.
// * Description:
// */
//public class BdASRRecognizer
//{
//
//    public static final int STATUS_NONE = 2;
//    public static final int STATUS_READY = 3;
//    public static final int STATUS_SPEAKING = 4;
//    public static final int STATUS_RECOGNITION = 5;
//    public static final int STATUS_ERROR = 6;
//    public static final int STATUS_SUCCESS = 7;
//    public static final int STATUS_FINISHED = 8;
//    public static final int STATUS_WAITING_READY = 8001;
//
//    private Handler mHandler = new Handler(Looper.myLooper());
//
//    private static BdASRRecognizer instance;
//    /**
//     * 识别的引擎当前的状态
//     */
//    protected int status = STATUS_NONE;
//    /**
//     * SDK 内部核心 EventManager 类
//     */
//    private EventManager asr;
//    /**
//     * SDK 内部核心 事件回调类， 用于开发者写自己的识别回调逻辑
//     */
//    private EventListener eventListener;
//    private List<Callback> callbacks;
//    private String params;
//
//    private BdASRRecognizer()
//    {
//        callbacks = new ArrayList<>();
//        OnlineShortRecognizeParams p = new OnlineShortRecognizeParams();
//        params = new JSONObject(p).toString();
//        eventListener = new RecogEventAdapter(new Impl());
//    }
//
//    public static BdASRRecognizer getInstance()
//    {
//        if (instance == null)
//        {
//            synchronized (BdASRRecognizer.class)
//            {
//                if (instance == null)
//                {
//                    instance = new BdASRRecognizer();
//                }
//            }
//        }
//        return instance;
//    }
//
//    private final AtomicBoolean isInited = new AtomicBoolean(false);
//    public void init(Context context)
//    {
//        if (isInited.getAndSet(true)) return;
//
//        asr = EventManagerFactory.create(context, "asr");
//        asr.registerListener(eventListener);
//    }
//
//    public void destroy()
//    {
//        if(isInited.getAndSet(false)){
//            if (asr == null) {
//                return;
//            }
//            cancel();
//            // SDK 集成步骤（可选），卸载listener
//            asr.unregisterListener(eventListener);
//            asr = null;
//            status = STATUS_NONE;
//        }
//    }
//
//    public void registerListener(Callback callback)
//    {
//        callbacks.remove(callback);
//        callbacks.add(callback);
//    }
//
//    public void unregisterListener(Callback callback)
//    {
//        callbacks.remove(callback);
//    }
//
//    public void start()
//    {
//        if(asr != null && status == STATUS_NONE){
//            L.INSTANCE.e("调用asr start");
//            asr.send(SpeechConstant.ASR_START, params, null, 0, 0);
//        }
//    }
//
//    /**
//     * 提前结束录音等待识别结果。
//     */
//    public void stop()
//    {
//        if(asr != null){
//            L.INSTANCE.e("调用asr stop");
//            asr.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
//        }
//    }
//
//    /**
//     * 取消本次识别，取消后将立即停止不会返回识别结果。
//     * cancel 与stop的区别是 cancel在stop的基础上，完全停止整个识别流程，
//     */
//    public void cancel()
//    {
//        if (asr != null)
//        {
//            asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
//        }
//    }
//
//    private class Impl implements IRecogListener
//    {
//
//        @Override
//        public void onAsrReady()
//        {
//            status = STATUS_READY;
//        }
//
//        @Override
//        public void onAsrBegin()
//        {
//            status = STATUS_SPEAKING;
//            for (Callback c : callbacks){
//                c.onAsrStart();
//            }
//        }
//
//        @Override
//        public void onAsrEnd()
//        {
//            status = STATUS_RECOGNITION;
//        }
//
//        @Override
//        public void onAsrPartialResult(String[] results, RecogResult recogResult)
//        {
//            status = STATUS_SUCCESS;
//            L.INSTANCE.e("实时结果：" + results[0]);
//            mHandler.post(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    for (Callback c : callbacks){
//                        c.onPortraitResult(results[0]);
//                    }
//                }
//            });
//        }
//
//        @Override
//        public void onAsrFinalResult(String[] results, RecogResult recogResult)
//        {
//            status = STATUS_SUCCESS;
//            L.INSTANCE.e("最终结果：" + results[0]);
//            mHandler.post(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    for (Callback c : callbacks){
//                        c.onSuccess(results[0]);
//                    }
//                }
//            });
//        }
//
//        @Override
//        public void onAsrFinish(RecogResult recogResult)
//        {
//            status = STATUS_FINISHED;
//        }
//
//        @Override
//        public void onAsrFinishError(int errorCode, int subErrorCode, String errorMessage, String descMessage, RecogResult recogResult)
//        {
//            status = STATUS_ERROR;
//
//            L.INSTANCE.e("识别出错: " + descMessage + " code = " + errorCode + "-- subcode = " + subErrorCode);
//            mHandler.post(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    for (Callback c : callbacks){
//                        if(subErrorCode == -3005 || subErrorCode == 3301){
//                            c.onLowQualityVoice();
//                        }else{
//                            c.onFailed(descMessage, errorCode, subErrorCode);
//                        }
//                    }
//                }
//            });
//        }
//
//        /**
//         * 长语音识别结束
//         */
//        @Override
//        public void onAsrLongFinish()
//        {
//            status = STATUS_FINISHED;
//        }
//
//        @Override
//        public void onAsrVolume(int volumePercent, int volume)
//        {
//        }
//
//        @Override
//        public void onAsrAudio(byte[] data, int offset, int length)
//        {
//            for (Callback c : callbacks){
//                c.onAsrAudio(data, offset, length);
//            }
////            if (offset != 0 || data.length != length)
////            {
////                byte[] actualData = new byte[length];
////                System.arraycopy(data, 0, actualData, 0, length);
////                data = actualData;
////            }
//        }
//
//        @Override
//        public void onAsrExit()
//        {
//            status = STATUS_NONE;
//            L.INSTANCE.e("asr 调用结束");
//            mHandler.post(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    for (Callback c : callbacks){
//                        c.onComplete();
//                    }
//                }
//            });
//        }
//
//        @Override
//        public void onAsrOnlineNluResult(String nluResult)
//        {
//            status = STATUS_SUCCESS;
//            L.INSTANCE.e("onAsrOnlineNluResult：" + nluResult);
//        }
//
//        @Override
//        public void onOfflineLoaded()
//        {
//            L.INSTANCE.e("装载离线识别");
//        }
//
//        @Override
//        public void onOfflineUnLoaded()
//        {
//            L.INSTANCE.e("卸载离线识别");
//        }
//    }
//
//    public static abstract class Callback
//    {
//        public abstract void onSuccess(String result);
//
//        public abstract void onPortraitResult(String s);
//
//        public void onLowQualityVoice(){
//            L.INSTANCE.e("音频质量差");
//        }
//
//        public abstract void onFailed(String msg, int code, int subCode);
////
//        public abstract void onComplete();
////
//        public void onAsrStart(){}
//
//        public void onAsrAudio(byte[] data, int offset, int length){}
////
////        public void onAsrVolume(int volumePercent, int volume){}
//    }
//}
