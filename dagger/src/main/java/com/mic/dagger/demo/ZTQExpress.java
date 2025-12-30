package com.mic.dagger.demo;

import dagger.Component;

/**
 * 依赖注入器，有了需求方和供应方，那么就需要将两者连接起来，依赖对象只有从供应方交给需求方，才有意义
 *
 * 1.注意依赖注入器是一个 interface 而非 class，在编译时，Dagger 会生成对应的实现类
 * 2.这个接口添加了一个注解：@Component，这个注解是就是告诉注入器，从哪个依赖供应方拿依赖对象。
 * 这段代码里，@Component 注解告知了中通，去淘宝这个供应商拿到电脑并快递给买家
 *
 * 3.@Component 这个注解的 modules 属性是一个 Class<?>[] 数组，因此可以让依赖注入器指定不止一个依赖供应方。
 * 例如，这个例子中，中通不仅可以从淘宝拿电脑进行配送，也可以从京东拿电脑：
 */

@Component(modules = {TaoBao.class, JD.class})
public interface ZTQExpress {
    void deliverTo(Person person);
}
