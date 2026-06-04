package com.mic.dagger.demo.d03_depends;

import dagger.Module;
import dagger.Provides;

/**
 * @Module 用于告知 Dagger 这个类是一个依赖提供商，这样 Dagger 才能够识别
 */
@Module
public class TaoBao {

    //组装一台电脑
    private Computer assembleComputer(CPU cpu) {
        Computer computer = new Computer("淘宝组装的电脑", cpu);
        return computer;
    }

    @Provides
    public Computer getComputer(CPU cpu) {
        return assembleComputer(cpu);
    }

    @Provides
    public CPU getCPU() {
        return new CPU("AMD");
    }
}