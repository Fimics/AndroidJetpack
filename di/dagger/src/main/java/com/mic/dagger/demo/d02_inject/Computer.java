package com.mic.dagger.demo.d02_inject;

import javax.inject.Inject;

public class Computer {

    private String name;

    @Inject
    public Computer() {
        this.name = "自我构造的电脑";
    }

    public Computer(String name) {
        this.name = name;
    }

    public void play(String game) {
        System.out.println("使用 " + name + " 玩 " + game);
    }
}