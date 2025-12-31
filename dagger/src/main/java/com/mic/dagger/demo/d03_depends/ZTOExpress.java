package com.mic.dagger.demo.d03_depends;


import dagger.Component;

/**
 * 这个接口添加了一个注解：@Component，这个注解是就是告诉注入器，从哪个依赖供应方拿依赖对象。这段代码里，@Component 注解告知了中通，去淘宝这个供应商拿到电脑并快递给买家。
 *
 * @Component 这个注解的 modules 属性是一个 Class<?>[] 数组，因此可以让依赖注入
 */
@Component(modules = TaoBao.class)
public interface ZTOExpress {
    void deliverTo(Person person);
}