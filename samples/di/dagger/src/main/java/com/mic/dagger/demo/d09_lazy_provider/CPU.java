package com.mic.dagger.demo.d09_lazy_provider;

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