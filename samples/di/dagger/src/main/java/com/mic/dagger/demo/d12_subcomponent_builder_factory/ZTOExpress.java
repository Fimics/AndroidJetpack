package com.mic.dagger.demo.d12_subcomponent_builder_factory;


import dagger.Component;


@SanScope
@Component(modules = {TaoBao.class} ,dependencies = UPSExpress.class) // 移除CPUProvider
public interface ZTOExpress {
    ZTOShanghaiExpress.Factory getShanghaiDepartment();
}