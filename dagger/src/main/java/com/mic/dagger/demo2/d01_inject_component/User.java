package com.mic.dagger.demo2.d01_inject_component;

import javax.inject.Inject;

public class User {

    // 1.使用Inject 注解在构造方法上， 就是告诉Dagger 可以通过构造方法来创建并获取到User的实例
    @Inject
    public User(){

    }
}
