package com.mic.dagger.demo.d03_depends;

import javax.inject.Inject;

public class Person {

    private String name;
    @Inject
    public Computer computer;

   public Person(String name){
       this.name = name;
   }

    public void playGame(String gameName) {
        System.out.print(name + "\n\t");
        computer.play(gameName);
    }
}