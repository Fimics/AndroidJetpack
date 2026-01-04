package com.mic.dagger.demo.d11_component_factory;


import dagger.Component;


@SanScope
@Component(modules = {TaoBao.class} ,dependencies = UPSExpress.class) // 移除CPUProvider
public interface ZTOExpress {
    ZTOShanghaiExpress getShanghaiDepartment();
}