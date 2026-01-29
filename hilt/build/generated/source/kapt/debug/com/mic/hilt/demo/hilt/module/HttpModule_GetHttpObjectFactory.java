package com.mic.hilt.demo.hilt.module;

import com.mic.hilt.demo.hilt.object.HttpObject;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("dagger.hilt.android.scopes.ActivityScoped")
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
public final class HttpModule_GetHttpObjectFactory implements Factory<HttpObject> {
  private final HttpModule module;

  private HttpModule_GetHttpObjectFactory(HttpModule module) {
    this.module = module;
  }

  @Override
  public HttpObject get() {
    return getHttpObject(module);
  }

  public static HttpModule_GetHttpObjectFactory create(HttpModule module) {
    return new HttpModule_GetHttpObjectFactory(module);
  }

  public static HttpObject getHttpObject(HttpModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.getHttpObject());
  }
}
