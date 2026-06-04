package com.mic.dagger.demo.d08_sub_component;

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