package com.yingding.lib_net.api;

import com.yingding.lib_net.bean.base.BaseResBean;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by Chuyh on 2017/11/14.
 */
public interface ExampleService {
    @GET(ExampleUrl.ExampleGet)
    Observable<BaseResBean<List<String>>> doExampleGet();
}
