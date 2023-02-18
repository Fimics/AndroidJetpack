package com.hnradio.common.util.bdaudio

data class PCM(var size : Int, var shortData : ShortArray) {

    companion object{
        @JvmStatic
        fun buildEmptyData() : PCM {
            return PCM(0, shortArrayOf(0))
        }


        @JvmStatic
        fun buildStartData() : PCM {
            return PCM(-1, shortArrayOf(0))
        }

        @JvmStatic
        fun buildCompleteData() : PCM {
            return PCM(-2, shortArrayOf(0))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PCM

        if (size != other.size) return false
        if (!shortData.contentEquals(other.shortData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + shortData.contentHashCode()
        return result
    }

}