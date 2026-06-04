package com.mic.guide.arch.mvp

/**
 * MVP 中 View 层契约的基接口。各页面定义自己的 View 接口继承它，
 * 声明 Presenter 可回调的视图方法（如 showLoading / showData / showError）。
 */
interface MvpView
