package com.mic.dagger.demo.d12_subcomponent_builder_factory;


public class Computer {

    private String name;

    private CPU cpu;
    private HardDisk hardDisk;        //电脑的硬盘

    public Computer(String name, CPU cpu, HardDisk hardDisk) {
        this.name = name;
        this.cpu = cpu;
        this.hardDisk = hardDisk;
    }

    public void play(String game) {
        System.out.println("使用 " + name + "(" + cpu + "，" + hardDisk + ") 玩 " + game);
    }
}