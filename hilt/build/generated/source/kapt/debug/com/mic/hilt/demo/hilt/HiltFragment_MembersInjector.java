package com.mic.hilt.demo.hilt;

import com.mic.hilt.demo.hilt.di.IInterface;
import com.mic.hilt.demo.hilt.object.HttpObject;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.Nullable;

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
public final class HiltFragment_MembersInjector implements MembersInjector<HiltFragment> {
  private final Provider<HttpObject> httpObjectProvider;

  private final Provider<IInterface> iiProvider;

  private HiltFragment_MembersInjector(Provider<HttpObject> httpObjectProvider,
      Provider<IInterface> iiProvider) {
    this.httpObjectProvider = httpObjectProvider;
    this.iiProvider = iiProvider;
  }

  @Override
  public void injectMembers(HiltFragment instance) {
    injectHttpObject(instance, httpObjectProvider.get());
    injectIi(instance, iiProvider.get());
  }

  public static MembersInjector<HiltFragment> create(Provider<HttpObject> httpObjectProvider,
      Provider<IInterface> iiProvider) {
    return new HiltFragment_MembersInjector(httpObjectProvider, iiProvider);
  }

  @InjectedFieldSignature("com.mic.hilt.demo.hilt.HiltFragment.httpObject")
  public static void injectHttpObject(HiltFragment instance, @Nullable HttpObject httpObject) {
    instance.httpObject = httpObject;
  }

  @InjectedFieldSignature("com.mic.hilt.demo.hilt.HiltFragment.ii")
  public static void injectIi(HiltFragment instance, @Nullable IInterface ii) {
    instance.ii = ii;
  }
}
