package com.mic.dagger.demo.d02_inject;

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

    // 现在淘宝这个供应商不再提供 Compupter
    @Provides
    public String getComputer() {
        return "";
    }
}