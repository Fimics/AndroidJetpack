package com.mic.nav.chat;

public class ChatMessage {
    public  String text;
    public  boolean isQuestion;
    public  boolean isLast;
    public  String sid;

    public ChatMessage(String text, boolean isQuestion, boolean isLast, String sid) {
        this.text = text;
        this.isQuestion = isQuestion;
        this.isLast = isLast;
        this.sid = sid;
    }

    public String getText() {
        return text;
    }

    public boolean isQuestion() {
        return isQuestion;
    }

    public boolean isLast() {
        return isLast;
    }

    public String getSid() {
        return sid;
    }
}
