package com.hnradio.common.util.bdaudio


class BdAudioTransformer {


    companion object{
        val instance : BdAudioTransformer by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ BdAudioTransformer()}
    }

    private var mCallbackList : MutableList<Callback> = mutableListOf()
    fun registerCallback(callback : Callback){
        mCallbackList.remove(callback)
        mCallbackList.add(callback)
    }

    fun unregisterCallback(callback : Callback){
        mCallbackList.remove(callback)
    }

    interface Callback{
        fun onError(key : Int, msg : String)
        fun onSuccess(key : Int, ret : Pair<Int, String>)
    }

    /***
     * 常规查询
     */
    fun query(key : Int, url : String){
        BdRecorgnizeTask().recorgnize(key, url, C())
    }

    /**
     * 快速查询
     */
    fun fastQuery(userToken : String, key : Int, url : String){
        BdFastRecognizeTask().recorgnize(userToken, key, url, FC())
    }

    private inner class C : BdRecorgnizeTask.Callback{
        override fun onError(key: Int, msg: String) {
            mCallbackList.forEach {
                it.onError(key, msg)
            }
        }

        override fun onSuccess(key: Int, ret: Pair<Int, String>) {
            mCallbackList.forEach {
                it.onSuccess(key, ret)
            }
        }
    }

    private inner class FC : BdFastRecognizeTask.Callback{
        override fun onError(key: Int, msg: String) {
            mCallbackList.forEach {
                it.onError(key, msg)
            }
        }

        override fun onSuccess(key: Int, ret: Pair<Int, String>) {
            mCallbackList.forEach {
                it.onSuccess(key, ret)
            }
        }
    }
}