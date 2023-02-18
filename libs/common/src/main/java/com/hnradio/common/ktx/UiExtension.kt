package com.hnradio.common.ktx

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.hnradio.common.AppContext
import com.hnradio.common.util.ScreenUtils
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*

/**
 * Created by ytf on 2020/10/26 026.
 * Description:
 */
object UiExtension {

    val Int.C : Int
        get() = ContextCompat.getColor(AppContext.getContext(), this)

    val Int.S : String
        get() = AppContext.getContext().resources.getString(this)

    val Int.intArray :IntArray
        get() = AppContext.getContext().resources.getIntArray(this)

    val Int.strArray :Array<String>
        get() = AppContext.getContext().resources.getStringArray(this)

    val Int.D : Drawable
        get() =  ResourcesCompat.getDrawable(AppContext.getContext().resources, this, null)!!

    val Int.ResDimen : Int
        get() = AppContext.getContext().resources.getDimensionPixelOffset(this)

    val Int.DP : Int
        get() = ScreenUtils.dip2px(AppContext.getContext(), this.toFloat())

    val Float.DP : Float
        get() = ScreenUtils.dip2px(AppContext.getContext(), this).toFloat()

    /**
     * <img src="https://img.jbzj.com/file_images/article/201904/20194383748427.png?20193383759"/>
     * 中线到BOTTOM的距离是(Descent+Ascent+leading=0)/2 = 基线到中线的距离+Descent
     * */
    fun Paint.baseline() : Float{
        val fm = fontMetrics
        return (fm.descent - fm.ascent) / 2 - fm.descent
    }

    fun Paint.baseline(centerY: Float) : Float{
        val fm = fontMetrics
        return centerY + ((fm.descent - fm.ascent) / 2 - fm.descent)
    }

    fun View.HIDE(){
        this.visibility = View.GONE
    }

    fun View.INVISIBLE(){
        this.visibility = View.INVISIBLE
    }

    fun View.SHOW(){
        this.visibility = View.VISIBLE
    }

    fun View.click(callback: (v: View) -> Unit){
        this.setOnClickListener {
            callback(it)
        }
    }

    fun View.debounceClick(callback: (v: View) -> Unit){
        this.setOnClickListener(getDebounceListener(View.OnClickListener(callback)))
    }

    fun getDebounceListener(listener: View.OnClickListener) : View.OnClickListener{
        val h = DebounceClickHandler(listener)
        return Proxy.newProxyInstance(View.OnClickListener::class.java.classLoader,
                arrayOf<Class<*>>(View.OnClickListener::class.java), h) as View.OnClickListener
    }


    class DebounceClickHandler(var listener: View.OnClickListener) : InvocationHandler{

        private val interval = LongArray(2)

        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
            System.arraycopy(interval, 1, interval, 0, 1)
            interval[1] = SystemClock.uptimeMillis()
            if (interval[1] - interval[0]> 500) {
                // 调用方法,通过反射的形式来调用 mTarget 的 method
                method?.invoke(listener, args!![0])
            }
            return null
        }
    }

    @JvmStatic
    @BindingAdapter("visible")
    fun View.visible(yn : Boolean){
        visibility =if(yn) View.VISIBLE else View.GONE
    }
}