package com.mic.jetpack.room

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.fragment.app.Fragment
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mic.databinding.FragmentRoomBinding
import com.mic.jetpack.room.bean.User
import com.mic.jetpack.room.dao.UserDao
import com.mic.jetpack.room.database.AppDatabase
import com.mic.libcore.utils.KLog
import kotlin.concurrent.thread


class RoomFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!
    private val tag = "room"
    private var appDatabase:AppDatabase?=null
    private var userDao:UserDao?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDatabase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnInsertAll.setOnClickListener {
           userDao!!.insertAll(listOf(User(1,"li","pengju")))
        }
        binding.btnLoadIds.setOnClickListener {
            userDao!!.loadAllByIds(intArrayOf(1)).forEach {
                KLog.d(tag,it.toString())
            }
        }
        binding.btnFindName.setOnClickListener {
            val user =userDao!!.findByName("li","pengju")
            KLog.d(tag,user.toString())
        }
        binding.btnGetAll.setOnClickListener {
            userDao!!.getAll().forEach {
                KLog.d(tag,it.toString())
            }
        }
        binding.btnDelete.setOnClickListener {
            userDao!!.delete(User(1,"li","pengju"))
        }
        binding.btnUpdate.setOnClickListener {
            userDao!!.update(User(1,"li","pengju1"))
            userDao!!.getAll().forEach {
                KLog.d(tag,it.toString())
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initDatabase(){
        //1）.创建内存数据库,也就是说这种数据库当中存储的数据，只会存留在内存当中，进程被杀死之后，数据随之丢失
//                 Room.inMemoryDatabaseBuilder(getApplicationContext(),AppDatabase.class);
        Room.inMemoryDatabaseBuilder(requireActivity(),AppDatabase::class.java)
        //2）.创建本地持久化的数据库

        thread(start = true) {
           appDatabase=Room.databaseBuilder(requireActivity(),AppDatabase::class.java,"room")
               //是否允许在主线程上操作数据库，默认false。
               //相比sqlite法无明文禁止即可为来说，Room给出了规范
               .allowMainThreadQueries()
               //数据库创建和打开的事件会回调到这里，可以再次操作数据库
               .addCallback(CallBack())
               //指定数据查询数据时候的线程池,
               .setQueryExecutor(ArchTaskExecutor.getIOThreadExecutor())
               //可以利用它实现自定义的sqliteOpenHelper，来实现数据库的加密存储，默认是不加密的
//               .openHelperFactory
               //数据库升级 1---2
//               .addMigrations
               .build()
            userDao= appDatabase!!.userDao()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private inner class CallBack :RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            KLog.d(tag,"onCreate")
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            KLog.d(tag,"onOpen")
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            KLog.d(tag,"onDestructiveMigration")
        }
    }
}