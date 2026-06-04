package com.mic.dagger.demo.d12_subcomponent_builder_factory;

import javax.inject.Named;

import dagger.Component;

@Component(modules = CPUProvider.class)
public interface UPSExpress {
    @Named("AMD")
    CPU getAMDCPU();
    @Named("Intel")
    CPU getIntelCPU();

    @Component.Factory
    interface Factory {
        UPSExpress create(CPUProvider cpuProvider);
    }
}