package com.mic.aptmain;

public class MainActivity_MembersInjector {
    public static void inject(MainActivity mainActivity, User user) {
        mainActivity.user = user;
    }
}
