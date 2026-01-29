package com.mic.hilt.demo2;

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
public final class AppModule_ProvideStudentFactory implements Factory<Student> {
  private final AppModule module;

  private AppModule_ProvideStudentFactory(AppModule module) {
    this.module = module;
  }

  @Override
  public Student get() {
    return provideStudent(module);
  }

  public static AppModule_ProvideStudentFactory create(AppModule module) {
    return new AppModule_ProvideStudentFactory(module);
  }

  public static Student provideStudent(AppModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideStudent());
  }
}
