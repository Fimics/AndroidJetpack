package com.mic.dagger.demo.d10_component_builder;

import dagger.Component;

@SanScope
@Component(modules = {TaoBao.class}, dependencies = UPSExpress.class)
public interface ZTOExpress {
    void deliverTo(Person person);
//    ZTOShanghaiExpress.Factory getShanghaiDepartmentFactory();

    @Component.Builder
    interface Builder {
        Builder setTaoBao(TaoBao taoBao);
        Builder setUPSExpress(UPSExpress upsExpress);
        ZTOExpress build();
    }
}