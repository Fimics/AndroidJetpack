package com.mic.nav.volces.llm;

import android.annotation.SuppressLint;
import android.util.Pair;

import com.noetix.libcore.utils.KLog;

import java.util.ArrayDeque;

public class LLMHistory {
    private static final String TAG = "LLMHistory";
    private final int MAX_COUNT = 10;
    private final ArrayDeque<Pair<String, String>> history = new ArrayDeque<Pair<String, String>>(MAX_COUNT);


    private static final class Holder {
        private static final LLMHistory llmHistory = new LLMHistory();
    }

    public static LLMHistory getInstance() {
        return Holder.llmHistory;
    }

    @SuppressLint("NewApi")
    public void addHistory(String assistantContent, String userContent) {
        Pair pair = new Pair(userContent, assistantContent);
        int count = (int) history.stream().count();

        if (count>= MAX_COUNT){
            removeHistory();
        }
        KLog.d(TAG," count ->"+count);
        KLog.d(TAG,"userContent   "+userContent +  "assistantContent ->"+assistantContent );
        history.addLast(pair);
    }

    public void removeHistory(){
        history.removeFirst();
    }

    public ArrayDeque getHistory() {
        return history;
    }

    public void clean() {
        history.clear();
    }
}
