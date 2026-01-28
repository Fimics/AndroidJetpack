package com.mic.rx2.operators.op01_creation;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class c1_create {

    public static void main(String[] args) {

        new c1_create().create();

    }


    public void create(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("开始");
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(integer);
            }
            @Override
            public void onError(Throwable throwable) {
              System.out.println(throwable.getMessage());
            }

            @Override
            public void onComplete() {
               System.out.println("结束");
            }
        });
    }
}
