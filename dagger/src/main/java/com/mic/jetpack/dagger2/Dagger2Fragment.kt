package com.mic.jetpack.dagger2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.databinding.FragmentDagger2Binding
import com.mic.jetpack.dagger2.subcomponent.component.DaggerMainComponent
import com.mic.jetpack.dagger2.subcomponent.module.MainModule
//import com.mic.jetpack.dagger2.component.DaggerMyComponent
//import com.mic.jetpack.dagger2.component.DaggerPresenterComponent
//import com.mic.jetpack.DatabaseModule
//import com.mic.jetpack.HttpModule
//import com.mic.jetpack.DatabaseObject
//import com.mic.jetpack.HttpObject
//import com.mic.jetpack.PresenterObject
import com.mic.jetpack.dagger2.subcomponent.`object`.MainObject
import com.mic.jetpack.dagger2.subcomponent.`object`.SubObject
import com.mic.jetpack.dagger2.subcomponent.`object`.XOkhttp
import com.mic.jetpack.dagger2.subcomponent.`object`.XRetrofit
import com.mic.jetpack.dagger2.subcomponent.`object`.XUser
import com.mic.libcore.utils.KLog
import javax.inject.Inject
import javax.inject.Named
import dagger.Lazy
import javax.inject.Provider


class Dagger2Fragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentDagger2Binding? = null
    private val tag = "dagger"
    private val binding get() = _binding!!

//    @Inject
//    @JvmField
//     var httpObject: HttpObject?=null
////        get() = field
////        set(value) {
////            field=value
////        }
//
//
//    @Inject
//    @JvmField
//    var databaseObject: DatabaseObject?=null
//
//    @Inject
//    @JvmField
//    var presenterObject:PresenterObject?=null

    //以下为subComponent内容
    @Inject
    @JvmField
    var mainObject:MainObject?=null

    @Inject
    @JvmField
    var subObject:SubObject?=null

    @Named("key1")
    @Inject
    @JvmField
    var user1:XUser?=null

    @Named("key2")
    @Inject
    @JvmField
    var user2:XUser?=null


    @Inject
    @JvmField
    var xOkhttp:XOkhttp?=null

    @Inject
    @JvmField
    var xRetrofit:XRetrofit?=null

    //懒加载使用
    @Inject
    lateinit var lazy:Lazy<XRetrofit> //单列
    @Inject
    lateinit var provider:Provider<XRetrofit> //不是单列


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //单个component写法
//        DaggerMyComponent.create().injectDagger2Fragment(this)
        //多个component写法 有依赖关系 dependencies方式
//        DaggerMyComponent.builder()
//            .httpModule(HttpModule())
//            .databaseModule(DatabaseModule())
//            .presenterComponent(DaggerPresenterComponent.create())
//            .build()
//            .injectDagger2Fragment(this)

           DaggerMainComponent.builder()
               .mainModule(MainModule("jack","123456"))
               .build()
               .getSubComponent()
               .inject2Fragment(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDagger2Binding.inflate(inflater, container, false)
        binding.btnDagger2.setOnClickListener {
//            KLog.d(tag, "http code ${httpObject.hashCode()}")
//            KLog.d(tag, "database code ${databaseObject.hashCode()}")
//            KLog.d(tag, "presenter code ${presenterObject.hashCode()}")
//            KLog.d(tag,"---------------------------------------")
            KLog.d(tag, "main object code ${mainObject.hashCode()}")
            KLog.d(tag, "sub object ${subObject.hashCode()}")

            KLog.d(tag, "user1 name ${user1?.name}")
            KLog.d(tag, "user2 name  ${user2?.name}")
            KLog.d(tag, "user1 pwd ${user1?.pwd}")
            KLog.d(tag, "user2 pwd  ${user2?.pwd}")

            KLog.d(tag, "xokhttp code ${xOkhttp.hashCode()}")
            KLog.d(tag, "xretrofit code  ${xRetrofit.hashCode()}")

            KLog.d(tag, "lazy code ${lazy.hashCode()}")
            KLog.d(tag, "lazy code ${lazy.hashCode()}")
            KLog.d(tag, "provider code  ${provider.hashCode()}")
            KLog.d(tag, "provider code  ${provider.hashCode()}")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}