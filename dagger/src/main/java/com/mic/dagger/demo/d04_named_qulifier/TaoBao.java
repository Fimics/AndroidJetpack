package com.mic.dagger.demo.d04_named_qulifier;

import javax.inject.Named;
import javax.inject.Qualifier;

import dagger.Module;
import dagger.Provides;

/**
 * 那是因为 Dagger 是通过函数的返回值类型来判断应该调用哪个方法获取依赖对象的，你这两个方法虽然函数名不一样，
 * 但是返回值类型都是一样的，在这种情况下 Dagger 需要获取一个 Computer 但它是不知道应该调用哪个方法来获取 Computer 的。
 */
@Module
public class TaoBao {


    @Provides
//    @Named("台式机")
    @DesktopComputer
    public Computer getDesktop(CPU cpu) {
        return new Computer("淘宝的台式机", cpu);
    }

    @Provides
//    @Named("笔记本")
    @NotebookComputer
    public Computer getNotebook(CPU cpu) {
        return new Computer("淘宝的笔记本", cpu);
    }

    @Provides
    public CPU getCPU() {
        return new CPU("AMD");
    }
}