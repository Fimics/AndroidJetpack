package com.mic.hilt.demo.http.client;

import android.content.Context;
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
public final class VolleyProcessor_Factory implements Factory<VolleyProcessor> {
  private final Provider<Context> contextProvider;

  private VolleyProcessor_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VolleyProcessor get() {
    return newInstance(contextProvider.get());
  }

  public static VolleyProcessor_Factory create(Provider<Context> contextProvider) {
    return new VolleyProcessor_Factory(contextProvider);
  }

  public static VolleyProcessor newInstance(@Nullable Context context) {
    return new VolleyProcessor(context);
  }
}
