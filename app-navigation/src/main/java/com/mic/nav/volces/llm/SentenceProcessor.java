package com.mic.nav.volces.llm;


import com.jeremyliao.liveeventbus.LiveEventBus;
import com.noetix.libcore.event.AnswerEvent;
import com.noetix.libcore.utils.KLog;
import com.noetix.robotics.AppConfig;
import com.noetix.robotics.volces.tts.api.TTSApiObserver;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SentenceProcessor {
    private static final String TAG = "LLMApi";
    public int status = 0;
    private int seq = 0;
    private String sid ;
    private final StringBuilder buffer = new StringBuilder();
    private String prompt;
    private final StringBuffer mSentenceBuffer = new StringBuffer();
    // 定义句子结束符集合
    private static final Set<Character> END_PUNCTUATIONS = new HashSet<>(
            Arrays.asList('。', '？', '.', '。', '?', '！','~')
    );

    private TTSApiObserver ttsApiObserver;

    public SentenceProcessor() {
        if (!AppConfig.ttsUseXunFei()){
            ttsApiObserver = new TTSApiObserver();
            ttsApiObserver.observer();
        }
    }


    // 处理新接收到的数据块
    public void processChunk(LineResult result) {
        synchronized (buffer) {
            if (result.isEnd) {
                status=2;
                result.content = "。";

            }else {
                if (seq == 0){
                    status=0;
                    mSentenceBuffer.setLength(0);
                }else {
                    status=1;
                }
            }
            this.prompt=result.prompt;
            sid = result.sid;
            String chunk = result.content;
            buffer.append(chunk);
//            KLog.d(TAG, "processChunk: " + "result isEnd: "+result.isEnd+" status  "+status +"LineResult ->"+result.content);
            processBuffer();
        }
    }

    // 处理缓冲区中的数据，提取完整句子
    private void processBuffer() {
        while (true) {
            int splitIndex = -1;
            // 查找第一个出现的结束符
            for (int i = 0; i < buffer.length(); i++) {
                if (END_PUNCTUATIONS.contains(buffer.charAt(i))) {
                    splitIndex = i;
                    break; // 找到第一个结束符后停止
                }
            }

            if (splitIndex == -1) {
                break; // 无结束符，退出循环
            }

            // 提取完整句子并处理
            String sentence = buffer.substring(0, splitIndex + 1);
            handleCompleteSentence(sentence);

            // 移除已处理部分
            buffer.delete(0, splitIndex + 1);
        }
    }

    // 处理完整句子的方法（示例）
    private void handleCompleteSentence(String sentence) {
        // 实现你的业务逻辑，例如打印或存储
        KLog.d(TAG, "捕获到完整句子: " + sentence + " status：" + status+" sid "+sid+" seq "+seq+"  prompt ->"+prompt);
//        KLog.d(TAG,buffer.toString());
        AnswerEvent event = new AnswerEvent(sentence, sid, seq, status, "from");
        LiveEventBus.get(AnswerEvent.class).post(event);

        if (AppConfig.ttsUseXunFei()){
//            SpeechRecManager.instance().addTtsText(sentence,seq,status);
        }else {
            ttsApiObserver.addText(sentence,seq,status);
        }
        seq++;
        mSentenceBuffer.append(sentence);

        if (status==2){
           LLMHistory.getInstance().addHistory(mSentenceBuffer.toString(),prompt);
        }
    }

    public void reset() {
        status = 0;
        seq=0;
        mSentenceBuffer.setLength(0);
        buffer.setLength(0);
//        SpeechRecManager.instance().interrupt();
    }
}
