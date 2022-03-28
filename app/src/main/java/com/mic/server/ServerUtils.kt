package com.mic.server

import java.io.File

class ServerUtils {

    companion object{
        fun readText(path:String):String{
            val file = File(path);
            return  file.readText()
        }
    }
}