package com.mic.nav.volces.tts.api;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.noetix.libcore.utils.FileUtil;
import com.noetix.libcore.utils.KLog;
import com.noetix.libcore.utils.TaskExecutors;
import com.noetix.libnoetix.IRobotSDKManager;
import com.noetix.libnoetix.SDKContext;
import com.noetix.libnoetix.TTSHelper;
import com.noetix.libnoetix.entity.TTSFrame;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okio.ByteString;

public class TTSWebsocket {

    private static final String TAG = "TTSWebsocket";
    private final Commands commands = new Commands();
    private final ResponseHandler handler = new ResponseHandler();
    private String sessionId;
    private WebSocket mWebsocket;
    private volatile boolean isSessionStarted = false;
    private boolean isFirstFrame = true;
    private int completedIndex =0;

    public void connect() {
        tryFlushData();
        KLog.d(TAG,"connect......");
        sessionId = generateNewSessionId();
        Request request = new Request.Builder()
                .url(Config.URL)
                .header("X-Api-App-Key", Config.APP_ID)
                .header("X-Api-Access-Key", Config.TOKEN)
                .header("X-Api-Resource-Id", "volc.service_type.10029")
                .header("X-Api-Connect-Id", UUID.randomUUID().toString())
                .build();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(Level.HEADERS);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .pingInterval(50, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build();

        mWebsocket = okHttpClient.newWebSocket(request, new SocketListener());
    }

    public void disconnect() {

        tryFlushData();
        if (mWebsocket != null) {
            commands.finishConnection(mWebsocket);
            try {
                mWebsocket.close(1000, "Normal closure");
            } catch (Exception e) {
                KLog.e(TAG, "Error closing websocket", e);
            }
            mWebsocket = null;
        }
    }


    private void tryFlushData(){
        KLog.d(TAG,"构建最后的空Frame");
        byte [] audio = new byte[2048];
        long duration = TTSHelper.calculateAudioDuration(audio);
        TTSFrame ttsFrame = new TTSFrame(audio,2,duration);
        IRobotSDKManager.getInstance().processAudioStream(audio,2);
        KLog.d(TAG, "Received audio data, length: " +"  status ---------------------------->"+2);
    }

    private String generateNewSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private int status;
    private int seq;

    public void addText(String text, int seq, int status) {
        this.seq = seq;
        this.status = status;
        if (mWebsocket == null) {
            KLog.d(TAG, "WebSocket is null, attempting to reconnect");
            return;
        }

        if (!isSessionStarted) {
            KLog.d(TAG, "Session not started, ignoring text: " + text);
            return;
        }

        if (TextUtils.isEmpty(text)) {
            KLog.d(TAG, "Text to synthesize is empty");
            return;
        }

        KLog.d(TAG, "add Text " + text + "seq ->" + seq + "  status -> " + status);
        commands.sendMessage(mWebsocket, sessionId, text);
    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            KLog.d(TAG, "==> onOpen: X-Tt-Logid:" + response.header("X-Tt-Logid"));
            commands.startConnection(webSocket);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            KLog.d(TAG, "onMessage text: " + text);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            try {
                TTSResponse response = handler.parserResponse(bytes.toByteArray());
//                KLog.d(TAG, "==> response: " + response);

                if (response == null || response.optional == null) {
                    KLog.e(TAG, "Invalid response structure");
                    return;
                }

                switch (response.optional.event) {
                    case Event.EVENT_ConnectionFailed:
                    case Event.EVENT_SessionFailed:
                        handleSessionFailure();
                        break;
                    case Event.EVENT_ConnectionStarted:
                        handleConnectionStarted(webSocket);
                        break;
                    case Event.EVENT_SessionStarted:
                        handleSessionStarted();
                        break;
                    case Event.EVENT_TTSSentenceStart:
                        KLog.d(TAG, "Synthesis started... status ->" + status + "     seq " + seq);
                        break;
                    case Event.EVENT_TTSSentenceEnd:
                        completedIndex = completedIndex+1;
                        KLog.d(TAG, "Synthesis completed... status ->" + status + "   seq " + seq+"   completedIndex  "+completedIndex);
//                        if (completedIndex==seq){
//                            KLog.d(TAG,"构建最后的空Frame");
//                            byte [] audio = new byte[2048];
//                            long duration = TTSHelper.calculateAudioDuration(audio);
//                            TTSFrame ttsFrame = new TTSFrame(audio,status,duration);
//                            LiveEventBus.get(TTSFrame.class).post(ttsFrame);
//                            KLog.d(TAG, "Received audio data, length: " +"  status ---------------------------->"+status);
//                        }
                        break;
                    case Event.EVENT_TTSResponse:
                        handleTTSResponse(response);
                        break;
                    case Event.EVENT_ConnectionFinished:
                    case Event.EVENT_SessionFinished:
                        handleSessionFinished();
                        break;
                    default:
                        KLog.w(TAG, "Unhandled event: " + response.optional.event);
                        break;
                }
            } catch (Exception e) {
                KLog.e(TAG, "Error processing message", e);
            }
        }

        private void handleSessionFailure() {
            KLog.d(TAG, "Session/Connection failed");
            isSessionStarted = false;
        }

        private void handleConnectionStarted(WebSocket webSocket) {
            KLog.d(TAG, "EVENT_ConnectionStarted");
            commands.startTTSSession(webSocket, sessionId);
        }

        private void handleSessionStarted() {
            KLog.d(TAG, "EVENT_SessionStarted");
            isSessionStarted = true;
        }

        private void handleTTSResponse(TTSResponse response) {
//            KLog.d(TAG, "EVENT_TTSResponse");
            if (response.payload == null) {
                KLog.w(TAG, "Empty TTS response payload");
                return;
            }

            if (response.header != null && response.header.message_type == Protocol.AUDIO_ONLY_RESPONSE) {

                byte [] audio = response.payload;
                int status=0;
                if (isFirstFrame){
                    status=0;
                    isFirstFrame = false;
                }else {
                    status=1;
                }
                IRobotSDKManager.getInstance().processAudioStream(audio,status);
                KLog.d(TAG, "Received audio data, length: " + response.payload.length+"   status ---------------------------->"+status);
                if (SDKContext.isSaveAudioData) {
                        TaskExecutors.get().onIOTask(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.writeFile(
                                        audio,
                                        "/sdcard/tts_volces_data.pcm"
                                );
                            }
                        });
                }
                // 实际处理音频数据
            }
        }

        private void handleSessionFinished() {
            KLog.d(TAG, "Session finished");
            isSessionStarted = false;
            // 正常结束不自动重连
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            KLog.d(TAG, "onClosed: code=" + code + ", reason=" + reason);
            isSessionStarted = false;
            mWebsocket = null;
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            KLog.d(TAG, "onClosing: code=" + code + ", reason=" + reason);
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
            KLog.e(TAG, "onFailure: " + t.getMessage(), t);
            isSessionStarted = false;
            mWebsocket = null;
        }
    }
}
