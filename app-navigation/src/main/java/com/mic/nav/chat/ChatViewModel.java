package com.mic.nav.chat;

import android.text.TextUtils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.noetix.libcore.event.AnswerEvent;
import com.noetix.libcore.event.PreWakeUpEvent;
import com.noetix.libcore.event.SpeechDataEvent;
import com.noetix.libcore.utils.KLog;
import com.noetix.libcore.utils.TaskExecutors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatViewModel extends ViewModel {
    private static final String TAG = "nx_asr";
    private static final int CLEAN_DELAY = 30_000;

    private final MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>();
    private final List<ChatMessage> messageList = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, ChatMessage> activeQuestions = new HashMap<>(); // 跟踪活跃问题
    private final Map<String, ChatMessage> activeAnswers = new HashMap<>();   // 跟踪活跃答案
    private final Runnable cleanRunnable = this::cleanMessages;
    private long lastTime;

    public ChatViewModel() {
        chatMessages.setValue(new ArrayList<>());
    }

    public void init(LifecycleOwner owner) {
        observeEvents(owner);
    }

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    private void observeEvents(LifecycleOwner owner) {
        LiveEventBus.get(PreWakeUpEvent.class).observe(owner, this::handlePreWakeup);
        LiveEventBus.get(SpeechDataEvent.class).observe(owner, this::handleSpeechEvent);
        LiveEventBus.get(AnswerEvent.class).observe(owner, this::handleAnswerEvent);
    }

    private synchronized void handleSpeechEvent(SpeechDataEvent event) {
        if (TextUtils.isEmpty(event.content)) return;

        String sid = event.sid;
        ChatMessage existingQuestion = activeQuestions.get(sid);

        if (existingQuestion != null) {
            // 更新现有问题
            int index = messageList.indexOf(existingQuestion);
            if (index != -1) {
                ChatMessage updatedMsg = new ChatMessage(
                        event.content,
                        true,
                        event.ls,
                        sid
                );
                messageList.set(index, updatedMsg);
                activeQuestions.put(sid, updatedMsg);
                notifyUpdate();
            }
        } else {
            // 新问题
            ChatMessage newMessage = new ChatMessage(
                    event.content,
                    true,
                    event.ls,
                    sid
            );

            // 确保不会连续出现两个问题
            if (!endsWithQuestion()) {
                messageList.add(newMessage);
                activeQuestions.put(sid, newMessage);
                notifyUpdate();
            } else {
                KLog.w(TAG, "Skipped consecutive question for sid: " + sid);
            }
        }

        // 处理短语音自动清理
//        if (event.ls && event.content.length() < SpeechState.MIN_WORD) {
//            TaskExecutors.get().postDelayed(() -> removeMessage(sid), 500);
//        }

        if (event.ls) {
            KLog.d(TAG, "请求答案 -> prompt ->" + event.content);
            // 调用AI接口获取答案...
        }
    }

    private synchronized void handleAnswerEvent(AnswerEvent event) {
        if (TextUtils.isEmpty(event.text)) return;

        String sid = event.sid;
        ChatMessage existingAnswer = activeAnswers.get(sid);

        if (existingAnswer != null) {
            // 更新现有答案
            int index = messageList.indexOf(existingAnswer);
            if (index != -1) {
                String newText = existingAnswer.getText() + event.text;
                ChatMessage updatedMsg = new ChatMessage(
                        newText,
                        false,
                        true,
                        sid
                );
                messageList.set(index, updatedMsg);
                activeAnswers.put(sid, updatedMsg);
                notifyUpdate();
            }
        } else {
            // 新答案
            ChatMessage newAnswer = new ChatMessage(
                    event.text,
                    false,
                    true,
                    sid
            );

            // 确保不会连续出现两个答案
            if (!endsWithAnswer()) {
                messageList.add(newAnswer);
                activeAnswers.put(sid, newAnswer);

                // 关联问题标记为已完成
                ChatMessage question = activeQuestions.get(sid);
                if (question != null) {
                    activeQuestions.remove(sid);
                }

                notifyUpdate();
            } else {
                KLog.w(TAG, "Skipped consecutive answer for sid: " + sid);
            }
        }
    }

    // 检查最后一条消息是否是问题
    private boolean endsWithQuestion() {
        if (messageList.isEmpty()) return false;
        ChatMessage last = messageList.get(messageList.size() - 1);
        return last.isQuestion();
    }

    // 检查最后一条消息是否是答案
    private boolean endsWithAnswer() {
        if (messageList.isEmpty()) return false;
        ChatMessage last = messageList.get(messageList.size() - 1);
        return !last.isQuestion();
    }

    private void removeMessage(String sid) {
        synchronized (this) {
            boolean updated = false;

            // 移除问题
            ChatMessage question = activeQuestions.remove(sid);
            if (question != null && messageList.remove(question)) {
                updated = true;
            }

            // 移除答案
            ChatMessage answer = activeAnswers.remove(sid);
            if (answer != null && messageList.remove(answer)) {
                updated = true;
            }

            if (updated) {
                notifyUpdate();
            }
        }
    }

    private synchronized void handlePreWakeup(PreWakeUpEvent event) {
        TaskExecutors.get().removeCallback(cleanRunnable);
        if (!event.preWakeUp) {
            lastTime = System.currentTimeMillis();
            TaskExecutors.get().postDelayed(cleanRunnable, CLEAN_DELAY);
        } else {
            if (System.currentTimeMillis() - lastTime >= CLEAN_DELAY) {
                TaskExecutors.get().removeCallback(cleanRunnable);
            }
        }
    }

    private void notifyUpdate() {
        chatMessages.postValue(new ArrayList<>(messageList));
    }

    private void cleanMessages() {
        synchronized (this) {
            messageList.clear();
            activeQuestions.clear();
            activeAnswers.clear();
            notifyUpdate();
        }
    }

    public void cleanup() {
        cleanMessages();
        TaskExecutors.get().removeCallback(cleanRunnable);
    }
}
