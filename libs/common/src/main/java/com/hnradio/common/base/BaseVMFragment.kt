package com.hnradio.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hnradio.common.widget.AppLoading
import java.lang.reflect.ParameterizedType

abstract class BaseVMFragment<T : ViewDataBinding, VM : ViewModel> : Fragment() {

    protected var mViewModel: VM? = null

    lateinit var mBinding : T

    protected  fun < T : ViewDataBinding> binding(
        inflater: LayoutInflater,
        @LayoutRes layoutId: Int,
        container: ViewGroup?): T =   DataBindingUtil.inflate<T>(inflater,layoutId, container,false).apply {
        lifecycleOwner = this@BaseVMFragment
    }

    protected abstract val contentLayout : Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = binding(inflater,contentLayout,container)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val parameterizedType = javaClass.genericSuperclass
        if (parameterizedType is ParameterizedType)
        {
            val actualTypeArguments = parameterizedType.actualTypeArguments

            val modelType = actualTypeArguments[1] as Class<*>
            if (BaseViewModel::class.java.isAssignableFrom(modelType))
            {
                val mClass = modelType as Class<VM>
                mViewModel = if(useActivityViewModel){
                    requireActivity().viewModelStore.clear()
                    ViewModelProvider(requireActivity().viewModelStore, requireActivity().defaultViewModelProviderFactory)
                        .get(mClass)
                }else{
                    ViewModelProvider(this).get(mClass)
                }
                startObserve()
            }
        }
        setPageView()
    }

    open fun setPageView(){}

    open fun startObserve(){}

    open val useActivityViewModel : Boolean = false


    /**
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     * 加载框AppLoading
     * ---------------------------------------------------------------------------------------------
     * ---------------------------------------------------------------------------------------------
     */
    private var appLoading: AppLoading? = null

    fun showAppLoading(text: String? = "正在加载") {
        if (appLoading == null) appLoading = AppLoading(requireActivity())
        appLoading!!.show(text)
    }

    fun closeAppLoading() {
        appLoading?.hide()
    }

    fun showLockedAppLoading(text: String) {
        appLoading?.isLocked = false
        showAppLoading(text)
        appLoading?.isLocked = true
    }

    fun closeLockedApploading() {
        appLoading?.isLocked = false
        closeAppLoading()
    }



    /**
     * https://www.jianshu.com/p/2201a107d5b5?utm_campaign=hugo
     * 是否执行懒加载
     */
    private var isLoaded = false

    /**
     * 当前Fragment是否对用户可见
     */
    private var isVisibleToUser = false

    /**
     * 当使用ViewPager+Fragment形式会调用该方法时，setUserVisibleHint会优先Fragment生命周期函数调用，
     * 所以这个时候就,会导致在setUserVisibleHint方法执行时就执行了懒加载，
     * 而不是在onResume方法实际调用的时候执行懒加载。所以需要这个变量
     */
    private var isCallResume = false

    /**
     * 是否调用了setUserVisibleHint方法。处理show+add+hide模式下，默认可见 Fragment 不调用
     * onHiddenChanged 方法，进而不执行懒加载方法的问题。
     */
    private var isCallUserVisibleHint = false

    override fun onResume() {
        super.onResume()
        isCallResume = true
        if (!isCallUserVisibleHint) isVisibleToUser = !isHidden
        judgeLazyInit()
    }

    private fun judgeLazyInit() {
        if (!isLoaded && isVisibleToUser && isCallResume) {
            lazyLoad()
            isLoaded = true
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isVisibleToUser = !hidden
        judgeLazyInit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
        isVisibleToUser = false
        isCallUserVisibleHint = false
        isCallResume = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        isCallUserVisibleHint = true
        judgeLazyInit()
    }

    /**
     * 懒加载
     */
    open fun lazyLoad() {}
}