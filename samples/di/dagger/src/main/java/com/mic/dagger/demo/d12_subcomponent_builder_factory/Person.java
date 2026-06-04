package com.mic.dagger.demo.d12_subcomponent_builder_factory;

import javax.inject.Inject;

public class Person {

    private String name;

    @Inject
    @DesktopComputer
    Computer desktop;

    @Inject
    @NotebookComputer
    Computer notebook;

    public Person(String name){
        this.name = name;
    }

    public void playGame(String gameName) {
        System.out.print(name + "\n");
        desktop.play("\t" + gameName);
        notebook.play("\t" + gameName);
    }
}