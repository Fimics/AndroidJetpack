package com.mic.nav.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.noetix.robotics.AppConfig;
import com.noetix.robotics.R;import com.noetix.robotics.chat.ChatMessage;

public class ChatAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private static final int TYPE_QUESTION = 0;
    private static final int TYPE_ANSWER = 1;

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatMessage>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessage oldItem,
                                               @NonNull ChatMessage newItem) {
                    return oldItem.getSid().equals(newItem.getSid());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessage oldItem,
                                                  @NonNull ChatMessage newItem) {
                    return oldItem.getText().equals(newItem.getText()) &&
                            oldItem.isQuestion() == newItem.isQuestion() &&
                            oldItem.isLast() == newItem.isLast();
                }
            };

    public ChatAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ANSWER) {
            return new QuestionViewHolder(inflater.inflate(R.layout.item_chat_robot, parent, false));
        } else {
            return new AnswerViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getItem(position);
        if (holder instanceof QuestionViewHolder) {
            ((QuestionViewHolder) holder).bind(message);
        } else {
            ((AnswerViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isQuestion() ? TYPE_QUESTION : TYPE_ANSWER;
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        QuestionViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.questionTextView);
        }

        void bind(ChatMessage message) {
            textView.setText(message.getText());
            // 可添加流式动画效果
        }
    }

    static class AnswerViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        AnswerViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.answerTextView);
        }

        void bind(ChatMessage message) {
            textView.setText(message.getText());
            // 可添加流式动画效果
        }
    }
}
