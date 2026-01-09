package com.mic.dagger.demo2.d01_inject_component;

import javax.inject.Inject;
import javax.inject.Singleton;

//@Singleton // 作用域 要么不写 要么与Component 作用域一致
public class User {

    // 1.使用Inject 注解在构造方法上， 就是告诉Dagger 可以通过构造方法来创建并获取到User的实例
//    @Inject
    public User(){

    }
}
