package com.mic.hilt.demo.http.client;

import android.app.Application;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.Nullable;

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
public final class XUtilsProcessor_Factory implements Factory<XUtilsProcessor> {
  private final Provider<Application> appProvider;

  private XUtilsProcessor_Factory(Provider<Application> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public XUtilsProcessor get() {
    return newInstance(appProvider.get());
  }

  public static XUtilsProcessor_Factory create(Provider<Application> appProvider) {
    return new XUtilsProcessor_Factory(appProvider);
  }

  public static XUtilsProcessor newInstance(@Nullable Application app) {
    return new XUtilsProcessor(app);
  }
}
