package com.mic.dagger.demo.d12_subcomponent_builder_factory;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class CPUProvider {

    @Provides
    @Named("AMD")
    public CPU getAMDCPU() {
        return new CPU("AMD");
    }

    @Provides
    @Named("Intel")
    public CPU getIntelCPU() {
        return new CPU("Intel");
    }
}