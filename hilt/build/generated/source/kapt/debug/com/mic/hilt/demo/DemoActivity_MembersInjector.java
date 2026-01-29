package com.mic.hilt.demo;

import com.mic.hilt.demo.http.annoation.BindXUtils;
import com.mic.hilt.demo.http.client.IHttpProcessor;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.Nullable;

@QualifierMetadata("com.mic.hilt.demo.http.annoation.BindXUtils")
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
public final class DemoActivity_MembersInjector implements MembersInjector<DemoActivity> {
  private final Provider<IHttpProcessor> httpProvider;

  private DemoActivity_MembersInjector(Provider<IHttpProcessor> httpProvider) {
    this.httpProvider = httpProvider;
  }

  @Override
  public void injectMembers(DemoActivity instance) {
    injectHttp(instance, httpProvider.get());
  }

  public static MembersInjector<DemoActivity> create(Provider<IHttpProcessor> httpProvider) {
    return new DemoActivity_MembersInjector(httpProvider);
  }

  @InjectedFieldSignature("com.mic.hilt.demo.DemoActivity.http")
  @BindXUtils
  public static void injectHttp(DemoActivity instance, @Nullable IHttpProcessor http) {
    instance.http = http;
  }
}
