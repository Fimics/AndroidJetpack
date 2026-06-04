package com.mic.dagger.demo.d09_lazy_provider;

import javax.inject.Inject;
import javax.inject.Provider; // 修改导入
import dagger.Lazy;

public class Person {

    private String name;

    public Person(String name){
        this.name = name;
    }

    @Inject
    @DesktopComputer
    Lazy<Computer> desktop;

    @Inject
    @NotebookComputer
    Lazy<Computer> notebook;

    @Inject
    Provider<Cola> cola; // 使用 javax.inject.Provider

    public void playGame(String gameName) {
        System.out.print(name + "\n");
        desktop.get().play("\t" + gameName+" this ->"+desktop.hashCode());
        notebook.get().play("\t" + gameName+ "this ->"+notebook.hashCode() );

        desktop.get().play("\t" + gameName+" this ->"+desktop.hashCode());
        notebook.get().play("\t" + gameName+ "this ->"+notebook.hashCode() );

        System.out.println("\t 喝了一瓶可乐："+cola.get());
        System.out.println("\t 再了一瓶可乐："+cola.get());
        System.out.println("\t 还了一瓶可乐："+cola.get());
    }
}