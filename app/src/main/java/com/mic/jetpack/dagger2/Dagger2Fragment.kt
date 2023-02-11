package com.mic.jetpack.dagger2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.databinding.FragmentDagger2Binding
import com.mic.jetpack.dagger2.component.DaggerMyComponent
import com.mic.jetpack.dagger2.component.DaggerPresenterComponent
import com.mic.jetpack.dagger2.module.DatabaseModule
import com.mic.jetpack.dagger2.module.HttpModule
import com.mic.jetpack.dagger2.`object`.DatabaseObject
import com.mic.jetpack.dagger2.`object`.HttpObject
import com.mic.jetpack.dagger2.`object`.PresenterObject
import com.mic.libcore.utils.KLog
import javax.inject.Inject


class Dagger2Fragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentDagger2Binding? = null
    private val tag = "dagger"
    private val binding get() = _binding!!

    @Inject
    @JvmField
     var httpObject: HttpObject?=null
//        get() = field
//        set(value) {
//            field=value
//        }


    @Inject
    @JvmField
    var databaseObject: DatabaseObject?=null

    @Inject
    @JvmField
    var presenterObject:PresenterObject?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //单个component写法
//        DaggerMyComponent.create().injectDagger2Fragment(this)
        //多个component写法 有依赖关系
        DaggerMyComponent.builder()
            .httpModule(HttpModule())
            .databaseModule(DatabaseModule())
            .presenterComponent(DaggerPresenterComponent.create())
            .build()
            .injectDagger2Fragment(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDagger2Binding.inflate(inflater, container, false)
        binding.btnDagger2.setOnClickListener {
            KLog.d(tag, "http code ${httpObject.hashCode()}")
            KLog.d(tag, "database code ${databaseObject.hashCode()}")
            KLog.d(tag, "presenter code ${presenterObject.hashCode()}")
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