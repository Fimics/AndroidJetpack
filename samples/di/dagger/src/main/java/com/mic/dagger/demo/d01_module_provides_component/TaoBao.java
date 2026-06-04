package com.mic.dagger.demo.d01_module_provides_component;

import dagger.Module;
import dagger.Provides;

/**
 * @Module 用于告知 Dagger 这个类是一个依赖提供商，这样 Dagger 才能够识别
 */
@Module
public class TaoBao {

    private Computer assembleComputer() {         //组装一台电脑
        Computer computer = new Computer("淘宝组装的电脑");
        return computer;
    }

    /**
     * Provides 用于告知 Dagger 这个依赖提供商里面哪些方法是用于提供依赖对象的。当 Dagger 需要创建一个依赖对象时，它会查找被 @Module 标识的类中被 @Provides 标识的方法，并根据所需依赖对象的类型，
     * @return
     */
    @Provides
    public Computer getComputer() {
        return assembleComputer();
    }
}