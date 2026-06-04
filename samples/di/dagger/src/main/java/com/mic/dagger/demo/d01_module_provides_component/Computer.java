package com.mic.dagger.demo.d01_module_provides_component;

public class Computer {

    private String name;

    public Computer(String name) {
        this.name = name;
    }

    public void play(String game) {
        System.out.println("使用 " + name + " 玩 " + game);
    }
}