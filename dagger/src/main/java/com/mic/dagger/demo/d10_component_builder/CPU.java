package com.mic.dagger.demo.d10_component_builder;

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