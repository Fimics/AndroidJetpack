package com.mic.jetpack.paging.source

import androidx.paging.DataSource
import com.mic.jetpack.paging.bean.Person
import com.mic.jetpack.paging.repository.DataRepository

class PersonDataSourceFactory: DataSource.Factory<Int, Person>() {
    override fun create(): DataSource<Int, Person> {
        return XItemDataSource(DataRepository())
    }
}