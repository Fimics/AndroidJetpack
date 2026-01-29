package com.mic.hilt.demo2;

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
public final class User_Factory implements Factory<User> {
  @Override
  public User get() {
    return newInstance();
  }

  public static User_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static User newInstance() {
    return new User();
  }

  private static final class InstanceHolder {
    static final User_Factory INSTANCE = new User_Factory();
  }
}
