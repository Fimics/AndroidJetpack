package com.mic.nav.volces.llm;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.noetix.libcore.http.OKHttpManager;
import com.noetix.libcore.utils.KLog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;

public class LLMApi {
    private final String TAG = "LLMApi";
    private final SentenceProcessor sentenceProcessor = new SentenceProcessor();
    private static final String BOTS_URL = "https://ark.cn-beijing.volces.com/api/v3/bots/chat/completions";
    private final OkHttpClient okHttpClient = OKHttpManager.get();
    private Call mCall;

    public void request(String prompt, String sid) {
//        if (mCall!= null && !mCall.isCanceled()) {
//            mCall.cancel();
//        }
        String payLoad = LLMPayLoad.getPayLoad(prompt);
        KLog.d(TAG, "发送请求  prompt ->" + prompt);
        RequestBody requestBody = RequestBody.create(payLoad, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(BOTS_URL)
                .addHeader("Authorization", "Bearer " + LLMPayLoad.KEY)
                .post(requestBody)
                .build();


        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                KLog.d(TAG, "onFailure " + e.getMessage());

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    KLog.d(TAG, "响应失败: " + response.code() + " " + response.message());
                    return;
                }
                sentenceProcessor.reset();
                // 获取响应体的 BufferedSource
                BufferedSource source = response.body().source();

                try {
                    // 循环读取数据流
                    while (!source.exhausted()) {
                        // 每次读取一段内容（按行读取或按块读取）
                        String line = source.readUtf8Line();
                        if (!TextUtils.isEmpty(line)) {
//                            KLog.d(TAG,"line ->"+line +"   sid ->"+sid);
                            LineResult result = LineResult.fromString(line, sid);
                            result.prompt=prompt;
//                            KLog.d(TAG, "响应数据: " + result);
                            sentenceProcessor.processChunk(result);
                        }
                    }
                } catch (Exception e) {
                    KLog.d(TAG, "流式读取异常: " + e.getMessage());
//                    LineResult result = new LineResult();
//                    result.isEnd = true;
//                    sentenceProcessor.processChunk(result);
                } finally {
                    source.close();
                }
            }

        });
    }

}
