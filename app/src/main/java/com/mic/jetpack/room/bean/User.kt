package com.mic.jetpack.room.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity
class User {

    //1.）对于一个表来说，他必须存在一个不为空的主键 也就是必须要标记PrimaryKey和NonNull两个注解
    //PrimaryKey注解的`autoGenerate`属性意味该主键的值，是否由数据库自动生成
    //由于我们这里是字符串的主键key，所以我们想要自己指定他得值，
    //如果是Long,INT类型的主键key，可以选择由数据库自动生成
    @PrimaryKey(autoGenerate = true)
    @NotNull
    var uid:Int=0

    //2.）该字段在数据库表中的列名称，不指定的默认就等于该字段的名字
    @ColumnInfo(name ="first_name", defaultValue = "default_name")
    var firstName:String?=null

    @ColumnInfo(name = "last_name")
    var lastName:String?=null

    //3.)如果不想让该字段映射成表的列，可以使用该注解标记
    var nickName:String?=null

    constructor(uid:Int,firstName:String,lastName:String):super(){
        this.uid=uid
        this.firstName=firstName
        this.lastName=lastName
    }

    override fun toString(): String {
        return "User(uid=$uid, firstName=$firstName, lastName=$lastName, nickName=$nickName)"
    }

    //5.)对于一个Room数据库的表而言，还有很多其他注解和属性可以使用，诸如索引，
    // 外键，关系数据支持的特性room都支持。但对于客户端来说一般也用不到，以上这些就够用了。


}