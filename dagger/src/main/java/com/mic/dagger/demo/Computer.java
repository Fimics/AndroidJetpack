package com.mic.dagger.demo;

import javax.inject.Inject;

/**
 * 依赖对象
 */
public class Computer {

    private String name;
    private CPU cpu;

    /**
     * 如果被 @Inject 标识的构造函数要是依赖于其他的对象， Dagger 也会自动注入。也就是说，Dagger 提供了两种方式创建依赖对象：
     *
     * 调用被 @Inject 注解标识的构造方法
     * 调用被 @Module 注解的类中提供相应的 @Provides 方法
     * @param cpu
     */
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