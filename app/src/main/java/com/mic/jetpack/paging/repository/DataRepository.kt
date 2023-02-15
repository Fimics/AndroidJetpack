package com.mic.jetpack.paging.repository

import com.mic.jetpack.paging.bean.Person

class DataRepository {
    private val dataList: MutableList<Person> = ArrayList()

    init {
        for (i in 0..999) {
            val person = Person(i.toString(), i.toString(), i.toString())
            dataList.add(person)
        }
    }

    fun initData(size: Int): List<Person> {
        return dataList.subList(0, size)
    }

    fun loadPageData(page: Int, size: Int): List<Person>? {
        val totalPage: Int = if (dataList.size % size == 0) {
            dataList.size / size
        } else {
            dataList.size / size + 1
        }
        if (page > totalPage || page < 1) {
            return null
        }
        return if (page == totalPage) {
            dataList.subList((page - 1) * size, dataList.size)
        } else dataList.subList((page - 1) * size, page * size)
    }
}