package com.mic.jetpack.paging.source

import androidx.paging.ItemKeyedDataSource
import com.mic.jetpack.paging.bean.Person
import com.mic.jetpack.paging.repository.DataRepository
import com.mic.libcore.utils.KLog

/**
 * ItemKeyedDataSource<Key, Value>：适用于目标数据的加载依赖特定item的信息，
 * 即Key字段包含的是Item中的信息，比如需要根据第N项的信息加载第N+1项的数据，传参中需要传入第N项的ID时，
 * 该场景多出现于论坛类应用评论信息的请求。
 */
class XItemDataSource(dataRepository: DataRepository) : ItemKeyedDataSource<Int, Person>() {

    private lateinit var mDataRepository: DataRepository

    init {
        this.mDataRepository=dataRepository
    }

    // loadInitial 初始加载数据
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Person>) {
        val dataList:List<Person> =mDataRepository.initData(params.requestedLoadSize)
        callback.onResult(dataList)
    }

    override fun getKey(item: Person): Int {
        return System.currentTimeMillis().toInt()
    }

    // loadBefore 向前分页加载数据
    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Person>) {
        val dataList:List<Person>? =mDataRepository.loadPageData(params.key,params.requestedLoadSize)
        if (dataList != null) {
            KLog.d("paging->load before")
            callback.onResult(dataList)
        }
    }

    // loadAfter 向后分页加载数据
    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Person>) {
        //API的调用，paging帮我们做的事情是计算数值！
        //pageindex
        //API的调用，paging帮我们做的事情是计算数值！
        //pageindex
        val dataList: List<Person>? = mDataRepository.loadPageData(params.key, params.requestedLoadSize)
        if (dataList != null) {
            KLog.d("paging->load after")
            callback.onResult(dataList)
        }
    }

}