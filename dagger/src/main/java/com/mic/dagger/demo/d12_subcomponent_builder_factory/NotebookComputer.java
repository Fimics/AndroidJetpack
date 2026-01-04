package com.mic.dagger.demo.d12_subcomponent_builder_factory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface NotebookComputer {
}