package com.hnradio.common.base

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.hnradio.common.R
import com.hnradio.common.databinding.CommonTitleLayoutBinding
import com.hnradio.common.util.getMyColor
import com.hnradio.common.widget.AppLoading
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType


/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.base
 * @ClassName: BaseActivity
 * @Description: 基础activity
 * @Author: shaoguotong
 * @CreateDate: 2021/6/28 8:57 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/6/28 8:57 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
abstract class BaseActivity<T : ViewBinding, M : ViewModel> : AppCompatActivity() {
    protected lateinit var viewBinding: T
    lateinit var viewModel: M

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewBindingAndViewModel()
        initImmersionBar(ImmersionBar.with(this)).init()
        supportActionBar?.hide()
    }

    //初始化沉浸栏
    open fun initImmersionBar(immersionBar: ImmersionBar): ImmersionBar {
//        return immersionBar.statusBarColorInt(getTitleBgColor())
//            .statusBarDarkFont(true)
//            .fitsSystemWindows(true)
//            .supportActionBar(false)
        return if (commonTitleLayoutBinding != null) {
            immersionBar.titleBar(commonTitleLayoutBinding?.titleTopLayout).statusBarDarkFont(true)
        } else {
            immersionBar
        }
    }

    //fragment 调用activity
    open fun fragmentToActivity(message: String) {

    }

    //初始化 viewBing 以及viewModel
    @Suppress("UNCHECKED_CAST")
    private fun initViewBindingAndViewModel() {

        //反射拿到类型
        val type: ParameterizedType = javaClass.genericSuperclass as ParameterizedType
        try {
            val inflate: Method = (type.actualTypeArguments[0] as Class<ViewBinding>)
                .getDeclaredMethod(
                    "inflate", LayoutInflater::class.java,
                    ViewGroup::class.java,
                    Boolean::class.java
                )
            val baseLayoutViewBinding = getTitleViewBind()
            if (baseLayoutViewBinding == null) {
                viewBinding = inflate.invoke(null, layoutInflater, null, false) as T
                setContentView(viewBinding.root)
            } else {
                viewBinding = inflate.invoke(
                    null,
                    layoutInflater,
                    baseLayoutViewBinding.root,
                    true
                ) as T
                setContentView(baseLayoutViewBinding.root)
            }

            val viewModelClass = type.actualTypeArguments[1] as Class<ViewModel>
            viewModel = ViewModelProvider(this).get(viewModelClass) as M
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    open fun onBack(): Boolean {
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (onBack()) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    var commonTitleLayoutBinding: CommonTitleLayoutBinding? = null

    //获取titleViewBind
    open fun getTitleViewBind(): ViewBinding? {
        val viewBind =
            CommonTitleLayoutBinding.inflate(layoutInflater, null, false)
        commonTitleLayoutBinding = viewBind
        viewBind.titleTv.text = getTitleText()
        getTitleRightView()?.let {
            viewBind.rightLayout.addView(it)
            it.setOnClickListener { onTitleRightClick() }
        }
        viewBind.backTv.setOnClickListener {
            if (!onBack()) {
                onBackPressed()
            }
        }
        viewBind.titleTopLayout.setBackgroundColor(getTitleBgColor())
        return viewBind
    }

    //获取标题
    open fun getTitleText(): String {
        return title.toString()
    }

    //设置标题
    fun setTitleText(title: String) {
        commonTitleLayoutBinding?.titleTv?.text = title
    }

    open fun getTitleBgColor(): Int {
        return getMyColor(R.color.white)
    }

    //标题栏右侧布局
    open fun getTitleRightView(): View? {
        return null
    }

    //标题栏右侧布局点击事件
    open fun onTitleRightClick() {

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
        if (appLoading == null) appLoading = AppLoading(this)
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

    fun updateLockedAppLoading(text: String){
        if(appLoading?.isLocked == true){
            appLoading?.update(text)
        }
    }

    fun closeLockedApploading() {
        appLoading?.isLocked = false
        closeAppLoading()
    }

    override fun onStop() {
        super.onStop()
        isActive = false
    }

    override fun onRestart() {
        super.onRestart()
        isActive = true
    }

    protected var isActive = true


    fun showInfoDialog(
        title : String = "提示",
        msg : String = "",
        cancelAble : Boolean = true,
        pt : String? = "确定", ptc : (()->Unit)? = null,
        nt : String? = "", ntc : (()->Unit)? = null,
        onDismiss : (()->Unit)? = null
    ){
        val builder = CommonDialog.Builder(this).also {
            it.setContent(msg)
            it.setTitle(title)
            it.setCaceledable(cancelAble)
            if(!pt.isNullOrEmpty()){
                it.setPositiveBtn(pt, object: BaseDialog.BaseDialogClickListener.OnActiconListener{
                    override fun onClick() {
                        ptc?.invoke()
                    }
                })
            }
            if(!nt.isNullOrEmpty()){
                it.setNegativeBtn(nt, object: BaseDialog.BaseDialogClickListener.OnCancelListener{
                    override fun onClick() {
                        ntc?.invoke()
                    }
                })
            }
        }
        val dialog = builder.build()
        onDismiss?.let { dc ->
            dialog.setOnDismissListener {
                dc()
            }
        }
        dialog.show()
    }
}