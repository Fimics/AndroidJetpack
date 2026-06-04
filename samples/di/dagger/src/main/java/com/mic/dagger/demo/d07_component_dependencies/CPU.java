package com.mic.dagger.demo.d07_component_dependencies;

import javax.inject.Inject;

public class CPU {

    private String producer;



    public CPU(String producer) {
        this.producer = producer;
    }

    @Override
    public String toString() {
        return producer + " CPU";
    }
}