package com.mic.dagger.demo.d07_component_dependencies;

import javax.inject.Named;

import dagger.Component;

@Component(modules = CPUProvider.class)
public interface UPSExpress {

    @Named("AMD")
    CPU getAMDCPU();
    @Named("Intel")
    CPU getIntelCPU();
}