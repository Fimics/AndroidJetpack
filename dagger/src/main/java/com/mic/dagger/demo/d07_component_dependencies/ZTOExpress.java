package com.mic.dagger.demo.d07_component_dependencies;

import dagger.Component;

@SanScope
@Component(modules = {TaoBao.class}, dependencies = UPSExpress.class)
public interface ZTOExpress {
    void inject(Person person); // 改为标准的inject方法
}