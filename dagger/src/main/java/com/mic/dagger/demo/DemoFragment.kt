package com.mic.dagger.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.dagger.databinding.FragmentDemoBinding
import com.mic.dagger.demo.d07_component_dependencies.CPUProvider
import com.mic.dagger.demo.d07_component_dependencies.DaggerUPSExpress
import com.mic.dagger.demo.d07_component_dependencies.DaggerZTOExpress
import com.mic.dagger.demo.d07_component_dependencies.Person
import com.mic.dagger.demo.d07_component_dependencies.TaoBao


//import com.mic.dagger.demo.d06_scope.DaggerZTOExpress
//import com.mic.dagger.demo.d06_scope.Person
//import com.mic.dagger.demo.d06_scope.TaoBao

//import com.mic.dagger.demo.d05_singleton.DaggerZTOExpress
//import com.mic.dagger.demo.d05_singleton.Person
//import com.mic.dagger.demo.d05_singleton.TaoBao

//import com.mic.dagger.demo.d04_named_qulifier.DaggerZTOExpress
//import com.mic.dagger.demo.d04_named_qulifier.Person
//import com.mic.dagger.demo.d04_named_qulifier.TaoBao


//import com.mic.dagger.demo.d03_depends.DaggerZTOExpress
//import com.mic.dagger.demo.d03_depends.Person
//import com.mic.dagger.demo.d03_depends.TaoBao

//import com.mic.dagger.demo.d02_inject.DaggerZTOExpress
//import com.mic.dagger.demo.d02_inject.Person
//import com.mic.dagger.demo.d02_inject.TaoBao

//import com.mic.dagger.demo.d01_module_provides_component.DaggerZTOExpress
//import com.mic.dagger.demo.d01_module_provides_component.Person
//import com.mic.dagger.demo.d01_module_provides_component.TaoBao


class DemoFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentDemoBinding? = null
    private val tag = "DemoFragment"
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDemoBinding.inflate(inflater, container, false)
         binding.btnDagger2.setOnClickListener {
//             test_d01_module_provides_component()
//             test_d02_inject();
//             test_d03_depends();
//             test_d04_named_qulifier();
//               test_d05_singleton();
//                test_d06_scope();
             test_d07_component_dependencies();
         }
        return binding.root
    }

//    fun test_d01_module_provides_component(){
//        val person =  Person("张三");
//       //创建依赖注入器
//        val ztoExpress = DaggerZTOExpress.builder().taoBao(TaoBao()).build()
////       //通过中通这个依赖注入器，为张三提供一台电脑
//        ztoExpress.deliverTo(person);
////        //现在张三可以玩游戏了
//        person.playGame("赛博朋克2077");
//    }
//
//    fun test_d02_inject(){
//        val person =  Person("张三");
//        //创建依赖注入器
//        val ztoExpress = DaggerZTOExpress.builder().taoBao(TaoBao()).build()
////       //通过中通这个依赖注入器，为张三提供一台电脑
//        ztoExpress.deliverTo(person);
////        //现在张三可以玩游戏了
//        person.playGame("赛博朋克2077");
//    }

//    fun test_d03_depends(){
//        val person =  Person("张三");
//        //创建依赖注入器
//        val ztoExpress = DaggerZTOExpress.builder().taoBao(TaoBao()).build()
////       //通过中通这个依赖注入器，为张三提供一台电脑
//        ztoExpress.deliverTo(person);
////        //现在张三可以玩游戏了
//        person.playGame("赛博朋克2077");
//    }

//    fun test_d04_named_qulifier(){
//        val person =  Person("张三");
//        //创建依赖注入器
//        val ztoExpress = DaggerZTOExpress.builder().taoBao(TaoBao()).build()
////       //通过中通这个依赖注入器，为张三提供一台电脑
//        ztoExpress.deliverTo(person);
////        //现在张三可以玩游戏了
//        person.playGame("赛博朋克2077");
//    }

    /**
     * 可以看到是使用的相同的硬盘。这就是 @Singleton 注解的作用。在这里就表示，通过中通从淘宝上拿到的硬盘都是这一块。
     * 但是这样也不太对，中通肯定不止为张三配送，那它为李四配送的时候，岂不是也送的张三的硬盘？
     * 所以这时候就别用自带的 @Singleton 范围，而是自定义一个范围，也就是使用 @Scope 注解。
     * 现在我们就为张三创建一个专属的范围，通过这个例子咱们也会明白 @Scope 的使用了：
     */
//    fun test_d05_singleton(){
//        val person =  Person("张三");
//        //创建依赖注入器
//        val ztoExpress = DaggerZTOExpress.builder().taoBao(TaoBao()).build()
////       //通过中通这个依赖注入器，为张三提供一台电脑
//        ztoExpress.deliverTo(person);
////        //现在张三可以玩游戏了
//        person.playGame("赛博朋克2077");
//    }

//    fun test_d06_scope(){
//        val person =  Person("张三");
//        //创建依赖注入器
//        val ztoExpress = DaggerZTOExpress.builder().taoBao(TaoBao()).build()
////       //通过中通这个依赖注入器，为张三提供一台电脑
//        ztoExpress.deliverTo(person);
////        //现在张三可以玩游戏了
//        person.playGame("赛博朋克2077");
//    }

    fun test_d07_component_dependencies(){
        val person = Person("张三")
        // 创建依赖注入器
        val upsExpress = DaggerUPSExpress.builder()
            .cPUProvider(CPUProvider())
            .build()
        val ztoExpress = DaggerZTOExpress.builder()
            .taoBao(TaoBao())
            .uPSExpress(upsExpress)
            .build()
        // 通过依赖注入器为Person注入依赖
        ztoExpress.inject(person)
        // 现在张三可以玩游戏了
        person.playGame("赛博朋克2077")
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