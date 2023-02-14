package com.mic.jetpack.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mic.jetpack.room.bean.User

//被Dao注解标记了的类  都是数据库的操作类接口
//有接口必然就会有实现类
@Dao
interface UserDao {

    //1.）如果是插入数据,只需要标记上Insert注解，并指明插入数据时如果已存在一条主键一样的数据，执行什么策略
    //REPLACE: 直接替换老数据
    //ABORT:终止操作，并回滚事务，也就是老数据不影响
    //IGNORE:忽略冲突，但是会插入失败
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(userList: List<User>)

    //2.)常规查询操作，此时还是需要你写sql语句的
    @Query("SELECT * FROM user WHERE uid IN (:array)")
    fun loadAllByIds(array:IntArray):List<User>

    //2.)常规查询操作，此时还是需要你写sql语句的
    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first:String,last:String):User

    //3.) 高级查询操作，可以通过livedata 以观察者的形式获取数据库数据，可以避免不必要的npe
    //更重要的是 他可以监听数据库表中的数据的比变化。一旦发生了 insert update delete。
    //room会自动读取表中最新的数据，发送给UI层 刷新页面
    //这一点是我们着重要关注的，看它背后有什么骚操作。
    @Query("SELECT * FROM user")
    fun getAll():List<User>

    //4.）删除操作非常简单 ，也可以执行sql的删除数据
    @Delete
    fun delete(user:User)

    @Update
    fun update(user:User)
}