package com.mic.dagger.demo.d03_depends;

import javax.inject.Inject;

public class Computer {

    private String name;
    private CPU cpu;

    @Inject
    public Computer(CPU cpu) {
        this.name = "自我构造的电脑";
        this.cpu = cpu;
    }

    public Computer(String name, CPU cpu) {
        this.name = name;
        this.cpu = cpu;
    }

    public void play(String game) {
        System.out.println("使用 " + name + "(" + cpu + ") 玩 " + game);
    }
}