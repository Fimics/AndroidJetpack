package com.mic.jetpack.paging.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mic.jetpack.paging.bean.Person
import com.mic.jetpack.paging.source.PersonDataSourceFactory

class  PersonViewModel constructor():ViewModel() {

    private lateinit var pagedListLiveData:LiveData<PagedList<Person>>

    init {
        val factory:DataSource.Factory<Int,Person> = PersonDataSourceFactory()
        pagedListLiveData = LivePagedListBuilder<Int,Person>(factory,20).build()
    }

    fun getLiveData():LiveData<PagedList<Person>>{
        return pagedListLiveData
    }
}