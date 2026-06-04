package com.mic.dagger.demo.d04_named_qulifier;

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