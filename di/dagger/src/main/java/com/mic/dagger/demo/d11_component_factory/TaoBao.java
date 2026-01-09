package com.mic.dagger.demo.d11_component_factory;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class TaoBao {

    @Provides
    @SanScope
    public HardDisk getHardDisk() {
        return new HardDisk("希捷");
    }

    @Provides
    @DesktopComputer
    public Computer getDesktop(@Named("AMD") CPU cpu, HardDisk hardDisk) { // 明确使用AMD CPU
        return new Computer("淘宝的台式机", cpu, hardDisk);
    }

    @Provides
    @NotebookComputer
    public Computer getNotebook(@Named("Intel") CPU cpu, HardDisk hardDisk) { // 明确使用Intel CPU
        return new Computer("淘宝的笔记本", cpu, hardDisk);
    }
}