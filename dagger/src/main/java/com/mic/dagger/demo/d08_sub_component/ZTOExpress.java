package com.mic.dagger.demo.d08_sub_component;

import dagger.Component;

@SanScope
@Component(modules = {TaoBao.class}, dependencies = UPSExpress.class)
public interface ZTOExpress {
    ZTOShanghaiExpress getShanghaiDepartment();
}