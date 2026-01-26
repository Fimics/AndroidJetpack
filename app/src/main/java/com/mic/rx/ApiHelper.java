package com.mic.rx;

import android.annotation.SuppressLint;
import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;
import com.mic.rx.api.ProjectApi;
import com.mic.rx.bean.ProjectBean;
import com.mic.rx.bean.ProjectItem;
import com.mic.libcore.utils.KLog2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ApiHelper {

    private static String BASE_URL = "https://www.wanandroid.com/";
    private static ProjectApi api;

    static {
        api = RetrofitClient.getRetrofitClient(BASE_URL).create(ProjectApi.class);
    }

    @SuppressLint("CheckResult")
    public void getData(View view) {
        RxView.clicks(view)
                .throttleFirst(2000, TimeUnit.MILLISECONDS) //2s内响应一次
                .observeOn(Schedulers.io())
                .flatMap(new Function<Object, ObservableSource<ProjectBean>>() {
                    @Override
                    public ObservableSource<ProjectBean> apply(Object o) throws Exception {
                        return api.getProject();//主数据
                    }
                })
                .flatMap(new Function<ProjectBean, ObservableSource<ProjectBean.DataBean>>() {
                    @Override
                    public ObservableSource<ProjectBean.DataBean> apply(ProjectBean projectBean) throws Exception {
                        return Observable.fromIterable(projectBean.getData()); //自己搞一个发射器，发10次
                    }
                })
                .flatMap(new Function<ProjectBean.DataBean, ObservableSource<ProjectItem>>() {
                    @Override
                    public ObservableSource<ProjectItem> apply(ProjectBean.DataBean dataBean) throws Exception {
                        return api.getProjectItem(1, dataBean.getId());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ProjectItem>() {
                    @Override
                    public void accept(ProjectItem projectItem) throws Exception {
                        KLog2.d("accept item data"+projectItem);
                    }
                })
        ;

    }
}
