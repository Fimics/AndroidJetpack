package com.hnradio.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.components.SimpleImmersionFragment
import com.hnradio.common.widget.AppLoading
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.base
 * @ClassName: BaseFragment
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/6/28 8:59 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/6/28 8:59 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
open class BaseFragment<T : ViewBinding, M : ViewModel> : SimpleImmersionFragment() {
    protected lateinit var viewBinding: T
    protected var viewModel: M? = null

    //当fragment在activity中显示
    open fun onShowInActivity() {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initViewBindingAndViewModel(inflater, container)
    }
    override fun immersionBarEnabled(): Boolean {
        return false
    }
    override fun initImmersionBar() {
        ImmersionBar.with(this).keyboardEnable(true).init()
    }


    //初始化 viewBing 以及viewModel
    @Suppress("UNCHECKED_CAST")
    private fun initViewBindingAndViewModel(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        val type: ParameterizedType = javaClass.genericSuperclass as ParameterizedType
        try {
            val inflate: Method = (type.actualTypeArguments[0] as Class<ViewBinding>)
                .getDeclaredMethod(
                    "inflate",
                    LayoutInflater::class.java,
                    ViewGroup::class.java,
                    Boolean::class.java
                )

            val viewModelClass = type.actualTypeArguments[1] as Class<ViewModel>
            viewModel = ViewModelProvider(this).get(viewModelClass) as M?

            val baseLayoutViewBinding = getTitleViewBind()
            return if (baseLayoutViewBinding == null) {
                viewBinding = inflate.invoke(null, inflater, container, false) as T
                viewBinding.root
            } else {
                viewBinding = inflate.invoke(
                    null,
                    layoutInflater,
                    baseLayoutViewBinding.root,
                    true
                ) as T
                baseLayoutViewBinding.root
            }

        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return null
    }

    //获取titleViewBind
    open fun getTitleViewBind(): ViewBinding? {
        return null
    }

    open fun onBack(): Boolean {
        return false
    }

    open fun getTitle(): String {
        return ""
    }

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