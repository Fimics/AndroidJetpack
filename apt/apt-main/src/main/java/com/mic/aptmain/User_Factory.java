package com.mic.aptmain;

import com.mic.annotation.Provider;

public class User_Factory implements Provider<User> {
    @Override
    public User get() {
        return new User();
    }
}
