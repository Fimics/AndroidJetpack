package com.mic.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RxUtils {

    public final static <UD> ObservableTransformer<UD, UD> rxUd() {
        return new ObservableTransformer<UD, UD>() {
            @Override
            public ObservableSource<UD> apply(Observable<UD> upstream) {
                return  upstream
                        .subscribeOn(Schedulers.io())//给上面的代码分配异步线程
                        .observeOn(AndroidSchedulers.mainThread()) //给下面的代码分配主线程
                        .map(new Function<UD, UD>() {
                            @Override
                            public UD apply(UD ud) throws Exception {
                                return ud;
                            }
                        });
            }
        };
    }
}
