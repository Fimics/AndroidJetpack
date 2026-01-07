package com.mic.aptmain;

import com.mic.annotation.Component;

@Component
public interface TestComponent {


    User provideUser(); // 确保返回具体类型，而不是void或基本类型

    Student provideStudent();

    void inject(MainActivity activity); // 这个方法可能有问题，因为返回void
}