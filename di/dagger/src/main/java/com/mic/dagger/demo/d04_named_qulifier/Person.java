package com.mic.dagger.demo.d04_named_qulifier;

import javax.inject.Inject;
import javax.inject.Named;

public class Person {

    private String name;
    @Inject
//    @Named("笔记本")
    @DesktopComputer
    public Computer computer;

   public Person(String name){
       this.name = name;
   }

    public void playGame(String gameName) {
        System.out.print(name + "\n\t");
        computer.play(gameName);
    }
}