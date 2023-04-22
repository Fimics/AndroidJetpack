package com.mic.jnibase;


import android.util.Log;

public class Dog { // NewObject 调用我们的构造函数

    public Dog() { // <init>
        Log.d("Dog", "Dog init...");
    }

    public Dog(int n1) { // <init>
        Log.d("Dog", "Dog init... n1:" + n1);
    }

    public Dog(int n1, int n2) { // <init>
        Log.d("Dog", "Dog init... n1:" + n1 + " n2:" + n2);
    }

    public Dog(int n1, int n2, int n3) { // <init>
        Log.d("Dog", "Dog init... n1:" + n1 + " n2:" + n2 + " n3:" + n3);
    }

}
