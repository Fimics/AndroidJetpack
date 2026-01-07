package com.mic.aptmain;

import com.mic.annotation.Provider;

@SuppressWarnings("unused")
public class DaggerApplicationComponent {

    Provider<User> userProvider;
    public static DaggerApplicationComponent create() {
        return new DaggerApplicationComponent();
    }
    public DaggerApplicationComponent() {
        userProvider = new User_Factory();
    }

    public void inject(MainActivity activity) {
        MainActivity_MembersInjector.inject(activity, userProvider.get());

    }
}
