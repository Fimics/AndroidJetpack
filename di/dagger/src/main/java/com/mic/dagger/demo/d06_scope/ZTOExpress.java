package com.mic.dagger.demo.d06_scope;



import dagger.Component;

@SanScope //声明自己是一个单例作用域的容器。
@Component(modules = TaoBao.class)
public interface ZTOExpress {
    void deliverTo(Person person);
}