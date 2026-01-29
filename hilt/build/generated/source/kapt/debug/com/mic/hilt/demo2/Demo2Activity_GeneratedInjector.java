package com.mic.hilt.demo2;

import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.internal.GeneratedEntryPoint;
import javax.annotation.processing.Generated;

@OriginatingElement(
    topLevelClass = Demo2Activity.class
)
@GeneratedEntryPoint
@InstallIn(ActivityComponent.class)
@Generated("dagger.hilt.android.processor.internal.androidentrypoint.InjectorEntryPointGenerator")
public interface Demo2Activity_GeneratedInjector {
  void injectDemo2Activity(Demo2Activity demo2Activity);
}
