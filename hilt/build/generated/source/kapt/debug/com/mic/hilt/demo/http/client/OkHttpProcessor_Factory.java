package com.mic.hilt.demo.http.client;

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
public final class OkHttpProcessor_Factory implements Factory<OkHttpProcessor> {
  @Override
  public OkHttpProcessor get() {
    return newInstance();
  }

  public static OkHttpProcessor_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OkHttpProcessor newInstance() {
    return new OkHttpProcessor();
  }

  private static final class InstanceHolder {
    static final OkHttpProcessor_Factory INSTANCE = new OkHttpProcessor_Factory();
  }
}
