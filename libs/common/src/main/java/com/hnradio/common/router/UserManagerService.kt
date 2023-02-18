package com.hnradio.common.router

import com.alibaba.android.arouter.facade.template.IProvider
import com.hnradio.common.http.bean.UserInfo

/**
 *
 * @Description: 用户登录管理
 * @Author: huqiang
 * @CreateDate: 2021-07-22 11:52
 * @Version: 1.0
 */
interface UserManagerService : IProvider {
    fun getLocalUserInfo(): UserInfo?
    fun startLogin()
}