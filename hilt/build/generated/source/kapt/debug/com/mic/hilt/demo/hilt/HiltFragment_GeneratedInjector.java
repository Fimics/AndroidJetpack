package com.mic.hilt.demo.hilt;

import dagger.hilt.InstallIn;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.internal.GeneratedEntryPoint;
import javax.annotation.processing.Generated;

@OriginatingElement(
    topLevelClass = HiltFragment.class
)
@GeneratedEntryPoint
@InstallIn(FragmentComponent.class)
@Generated("dagger.hilt.android.processor.internal.androidentrypoint.InjectorEntryPointGenerator")
public interface HiltFragment_GeneratedInjector {
  void injectHiltFragment(HiltFragment hiltFragment);
}
