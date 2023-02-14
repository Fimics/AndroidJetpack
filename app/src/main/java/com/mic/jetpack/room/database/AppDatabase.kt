package com.mic.jetpack.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mic.jetpack.room.bean.User
import com.mic.jetpack.room.dao.UserDao

// // TypeConverters用以声明该数据库支持的类型转换，
// 比如下面定义的DateConvert里面就定义Date类型的字段，存储数据库的时候会被转换成Long,
// 而该字段被读取的时候，会被转换成Date类型

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao():UserDao
}