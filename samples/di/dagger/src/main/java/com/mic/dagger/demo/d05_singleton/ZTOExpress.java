package com.mic.dagger.demo.d05_singleton;


import javax.inject.Singleton;

import dagger.Component;

/**
 * 这个接口添加了一个注解：@Component，这个注解是就是告诉注入器，从哪个依赖供应方拿依赖对象。这段代码里，@Component 注解告知了中通，去淘宝这个供应商拿到电脑并快递给买家。
 *
 * @Component 这个注解的 modules 属性是一个 Class<?>[] 数组，因此可以让依赖注入
 *
 * Dagger 在创建依赖对象的时候，是一个递归的过程。在这里 Dagger 只是为 Person 进行依赖注入，
 * 但是在注入 Computer 的时候发现创建 Computer 还需要 CPU，那 Dagger 就先去创建 CPU 这个依赖对象，然后发现还需要 HardDisk 对象，
 * 那就再去找，两个都找到了，才能创建出来一个 Computer ，然后将这个 Computer注入给 Person 对象。因此依赖注入器
 */

@Singleton //声明自己是一个单例作用域的容器。
@Component(modules = TaoBao.class)
public interface ZTOExpress {
    void deliverTo(Person person);
}