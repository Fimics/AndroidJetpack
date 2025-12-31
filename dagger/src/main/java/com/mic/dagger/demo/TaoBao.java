package com.mic.dagger.demo;


import javax.inject.Named;

import dagger.Module;
import dagger.Provides;


/**
 * TaoBao 是依赖供应方：负责提供依赖对象，类似与实际编码中的工厂类
 */

/**
 * @Module 用于告知 Dagger 这个类是一个依赖提供商，这样 Dagger 才能够识别
 */
@Module
public class TaoBao {

//    private Computer assembleComputer() {         //组装一台电脑
//        Computer computer = new Computer("淘宝组装的电脑");
//        return computer;
//    }

    /**
     * @Provides 用于告知 Dagger 这个依赖提供商里面哪些方法是用于提供依赖对象的。
     * 当 Dagger 需要创建一个依赖对象时，它会查找被 @Module 标识的类中被 @Provides 标识的方法，
     * 并根据所需依赖对象的类型，来看这里面哪个方法返回的是所需要的依赖对象的类型并调用。
     * 在这个例子中，Dagger 要创建一个 Computer 对象
     * @return
     */
//    @Provides
//    public Computer getComputer() {
//        return assembleComputer();
//    }


    /**
     * 没有供应商提供电脑，Dagger 就直接通过 Computer 中被 @Inject 标注的构造方法 new 出来一个对象给张三了
     */
//    @Provides
//    public String getComputer() {
//        return "";
//    }

    /**
     * 依赖对象还依赖其他对象
     * -------------------------------------------------------------------------------
     */

    //组装一台电脑
//    private Computer assembleComputer(CPU cpu) {
//        Computer computer = new Computer("淘宝组装的电脑", cpu);
//        return computer;
//    }
//
//
//    /**
//     * 我们在 getComputer 这个 @Provides 方法添加了一个 CPU 类型的参数。这个时候 Dagger 在获取 Computer 时，
//     * 会发现这个方法依赖于 CPU 类型的参数，这时它会去找提供 CPU 的方法，并先把 CPU 拿到，
//     * 再交给提供 Computer 的方法，最终获取到一个 Computer，完成对张三的依赖注入。这是一个递归的过程
//     * @param cpu
//     * @return
//     */
//    @Provides
//    public Computer getComputer(CPU cpu) {
//        return assembleComputer(cpu);
//    }

    @Provides
    public CPU getCPU() {
        return new CPU("AMD");
    }

    /**
     *  @Named
     * 类型上再加限定：@Named 和 @Qulifier 注解的使用
     * -------------------------------------------------------------------------------
     * Dagger 需要获取一个 Computer 但它是不知道应该调用哪个方法来获取 Computer 的。
     * 这个时候，就需要使用 @Named 注解了，这个注解使 Dagger 可以在返回值类型一样的情况下，再继续判断 @Named 注解的 value 值
     */

//    @Provides
//    @Named("台式机")
//    public Computer getDesktop(CPU cpu) {
//        return new Computer("淘宝的台式机", cpu);
//    }
//
//    @Provides
//    @Named("笔记本")
//    public Computer getNotebook(CPU cpu) {
//        return new Computer("淘宝的笔记本", cpu);
//    }

    /**
     * -------------------------------------------------------------------------------
     * qulifier 这个单词在英语中就是限定器的意思，顾名思义，在 Dagger 里肯定就是在类型相同时再进一步做个限定。它是一个元注解，
     * @Named 就是继承于它。那我们怎么用这个注解呢？答案就是像 @Named一样，自定义一个注解继承 @Qualifier。
     */

    @Provides
    @DesktopComputer
    public Computer getDesktop(CPU cpu) {
        return new Computer("淘宝的台式机", cpu);
    }

    @Provides
    @NotebookComputer
    public Computer getNotebook(CPU cpu) {
        return new Computer("淘宝的笔记本", cpu);
    }
}
