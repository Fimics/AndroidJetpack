package com.mic.dagger.demo.d06_scope;


import dagger.Module;
import dagger.Provides;

/**
 * 那是因为 Dagger 是通过函数的返回值类型来判断应该调用哪个方法获取依赖对象的，你这两个方法虽然函数名不一样，
 * 但是返回值类型都是一样的，在这种情况下 Dagger 需要获取一个 Computer 但它是不知道应该调用哪个方法来获取 Computer 的。
 */
@Module
public class TaoBao {

    @Provides
    public CPU getCPU() {
        return new CPU("AMD");
    }

    @Provides
    @SanScope
    public HardDisk getHardDisk() {
        return new HardDisk("希捷");
    }

    @Provides
    @DesktopComputer
    public Computer getDesktop(CPU cpu, HardDisk hardDisk) {
        return new Computer("淘宝的台式机", cpu, hardDisk);
    }

    @Provides
    @NotebookComputer
    public Computer getNotebook(CPU cpu, HardDisk hardDisk) {
        return new Computer("淘宝的笔记本", cpu, hardDisk);
    }
}