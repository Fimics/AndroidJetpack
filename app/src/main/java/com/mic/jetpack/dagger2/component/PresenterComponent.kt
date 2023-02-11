package com.mic.jetpack.dagger2.component

import com.mic.jetpack.dagger2.module.PresenterModule
import com.mic.jetpack.dagger2.`object`.PresenterObject
import com.mic.jetpack.dagger2.scope.UserScope
import dagger.Component

//@UserScope
//@Component(modules = [PresenterModule::class])
interface PresenterComponent {
    //使用依赖关系，就不再使用这种语法
//    void inject(MainActivity activity);
//    fun providePresenter(): PresenterObject
}