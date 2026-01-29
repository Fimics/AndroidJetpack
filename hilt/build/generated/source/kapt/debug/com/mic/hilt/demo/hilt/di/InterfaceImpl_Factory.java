package com.mic.hilt.demo.hilt.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class InterfaceImpl_Factory implements Factory<InterfaceImpl> {
  @Override
  public InterfaceImpl get() {
    return newInstance();
  }

  public static InterfaceImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InterfaceImpl newInstance() {
    return new InterfaceImpl();
  }

  private static final class InstanceHolder {
    static final InterfaceImpl_Factory INSTANCE = new InterfaceImpl_Factory();
  }
}
