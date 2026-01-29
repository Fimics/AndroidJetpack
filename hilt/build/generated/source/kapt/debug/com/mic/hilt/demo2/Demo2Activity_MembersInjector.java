package com.mic.hilt.demo2;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class Demo2Activity_MembersInjector implements MembersInjector<Demo2Activity> {
  private final Provider<User> userProvider;

  private final Provider<Student> studentProvider;

  private Demo2Activity_MembersInjector(Provider<User> userProvider,
      Provider<Student> studentProvider) {
    this.userProvider = userProvider;
    this.studentProvider = studentProvider;
  }

  @Override
  public void injectMembers(Demo2Activity instance) {
    injectUser(instance, userProvider.get());
    injectStudent(instance, studentProvider.get());
  }

  public static MembersInjector<Demo2Activity> create(Provider<User> userProvider,
      Provider<Student> studentProvider) {
    return new Demo2Activity_MembersInjector(userProvider, studentProvider);
  }

  @InjectedFieldSignature("com.mic.hilt.demo2.Demo2Activity.user")
  public static void injectUser(Demo2Activity instance, User user) {
    instance.user = user;
  }

  @InjectedFieldSignature("com.mic.hilt.demo2.Demo2Activity.student")
  public static void injectStudent(Demo2Activity instance, Student student) {
    instance.student = student;
  }
}
