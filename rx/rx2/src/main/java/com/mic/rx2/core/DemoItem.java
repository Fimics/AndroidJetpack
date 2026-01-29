package com.mic.rx2.core;

public class DemoItem {
    public final String category;
    public final String title;
    public final DemoAction action;

    public DemoItem(String category, String title, DemoAction action) {
        this.category = category;
        this.title = title;
        this.action = action;
    }
}