package com.mic.dagger.demo.d11_component_factory;

public class HardDisk {

    private String name;

    public HardDisk(String name) {
        this.name = name;
    }

    @Override
    public String toString() {            //这里我们打印了地址
        return name + " 硬盘@" + Integer.toHexString(hashCode());
    }
}