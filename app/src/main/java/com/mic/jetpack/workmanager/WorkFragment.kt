package com.mic.jetpack.workmanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.mic.databinding.FragmentWorkBinding
import com.mic.libcore.utils.KLog2
import java.util.concurrent.TimeUnit


class WorkFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentWorkBinding? = null
    private val binding get() = _binding!!
    private val tag = "work-task"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWorkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doWork()
    }

    private fun doWork(){
        binding.btnOneTime.setOnClickListener {
            doeOneTimeWork()
        }

        binding.btnMulti.setOnClickListener {
            doMultiWork()
        }

        binding.btnRepeat.setOnClickListener {
            doRepeatWork()
        }

        binding.btnConstrains.setOnClickListener {
            doConstraintsWork()
        }
    }

    private fun doeOneTimeWork(){
        // 单一的任务  一次
        var request:OneTimeWorkRequest?=null
        val sendData:Data=Data.Builder().putString("data","one time").build()

        // 请求对象初始化
        //1.架构设计他的调用者不是当前进程
        //2.业务设计上的特色，
//            2.1 支持条件处理
        /**
         * WorkManger还提供了以下的约束作为Work执行的条件：
         *  setRequiredNetworkType：网络连接设置
         *  setRequiresBatteryNotLow：是否为低电量时运行 默认false
         *  setRequiresCharging：是否要插入设备（接入电源），默认false
         *  setRequiresDeviceIdle：设备是否为空闲，默认false
         *  setRequiresStorageNotLow：设备可用存储是否不低于临界阈值
         */
        request=OneTimeWorkRequest.Builder(WorkTask::class.java).setInputData(sendData).build()
        //id != 编号  =  状态概念
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(request.id)
            .observe(viewLifecycleOwner){
                KLog2.d(tag,"return result data->${it.outputData.getString("data")}")
                KLog2.d(tag,"workinfo state->${it.state.name}")
                if (it.state.isFinished){
                    KLog2.d(tag,"finished")
                }
            }

        //请求对象 加入到队列
        WorkManager.getInstance(requireContext()).enqueue(request)
    }

    private fun doMultiWork(){
        var request:OneTimeWorkRequest=OneTimeWorkRequest.Builder(WorkTask::class.java).build()
        var request1:OneTimeWorkRequest=OneTimeWorkRequest.Builder(WorkTask::class.java).build()
        var request2:OneTimeWorkRequest=OneTimeWorkRequest.Builder(WorkTask::class.java).build()
        // 顺序执行
        WorkManager.getInstance(requireContext()).beginWith(request)
            .then(request1)
            .then(request2)
            .enqueue();
    }

    @SuppressLint("InvalidPeriodicWorkRequestInterval")
    private fun doRepeatWork(){
        // 重复的任务  多次  循环  , 最少循环重复 15分钟每次（少于15分钟默认为15分钟）
        val periodRequest:PeriodicWorkRequest = PeriodicWorkRequest.Builder(WorkTask::class.java,20,TimeUnit.SECONDS)
            .addTag("period")
            .build()

        // 监听状态，在循环任务的情况下，一般不会调用
        //原因：默认识别是一次任务完成执行回调
        //但是重复任务，你可以看作为一次任务不会完结
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(periodRequest.id)
            .observe(requireActivity()){
                KLog2.d(tag,"workinfo state->${it.state.name}")
                if (it.state.isFinished){
                    KLog2.d(tag,"finished")
                }
            }

        WorkManager.getInstance(requireContext()).enqueue(periodRequest)
        //取消任务
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag("period")
    }

    private fun doConstraintsWork(){
        // 约束条件，必须满足我的条件，才能执行后台任务 （在连接网络，插入电源 并且 处于空闲时）  内部做了电量优化（Android App 不耗电）
        //使用workmanager能够更好地管理电量
        //备份  时间长，耗电量高
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        /**
         * 除了上面设置的约束外，WorkManger还提供了以下的约束作为Work执行的条件：
         *  setRequiredNetworkType：网络连接设置
         *  setRequiresBatteryNotLow：是否为低电量时运行 默认false
         *  setRequiresCharging：是否要插入设备（接入电源），默认false
         *  setRequiresDeviceIdle：设备是否为空闲，默认false
         *  setRequiresStorageNotLow：设备可用存储是否不低于临界阈值
         */

        val sendData:Data=Data.Builder().putString("data","doConstraintsWork").build()
        val request=OneTimeWorkRequest.Builder(WorkTask::class.java)
            .setConstraints(constraints)
            .setInputData(sendData)
            .build()

        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(request.id)
            .observe(viewLifecycleOwner){
                KLog2.d(tag,"return result data->${it.outputData.getString("data")}")
                KLog2.d(tag,"workinfo state->${it.state.name}")
                if (it.state.isFinished){
                    KLog2.d(tag,"finished")
                }
            }

        //请求对象 加入到队列
        WorkManager.getInstance(requireContext()).enqueue(request)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}