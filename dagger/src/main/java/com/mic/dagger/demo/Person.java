package com.mic.dagger.demo;

import javax.inject.Inject;

/**
 * 依赖需求方：就是需要依赖对象的那些类
 */
public class Person {

    private String name;
    //依赖对象
    @Inject
    public Computer computer;

    public Person(String name){
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void playGame(String gameName) {
        System.out.print(name + "\n\t");
        computer.play(gameName);
    }
}