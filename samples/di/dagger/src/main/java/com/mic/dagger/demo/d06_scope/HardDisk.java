package com.mic.dagger.demo.d06_scope;

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