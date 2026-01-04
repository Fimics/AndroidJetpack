package com.mic.dagger.demo2.d01_inject_component;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("/user/info")
    Call<String> getUserInfo();
}
