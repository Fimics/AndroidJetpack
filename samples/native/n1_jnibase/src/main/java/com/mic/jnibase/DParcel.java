package com.mic.jnibase;

public class DParcel {

    static  {
        System.loadLibrary("jnibase");
    }

    private long mNativePtr = 0;

    // 不给外界调用
    private DParcel(long mNativePtr) {
        this.mNativePtr = mNativePtr;
        this.mNativePtr = nativeCreate();
    }

    private static class MyObject {
        private static DParcel DPARCEL = new DParcel(0);
    }

    public static DParcel obtain() {
        return MyObject.DPARCEL;
    }

    public final void writeInt(int val) {
        nativeWriteInt(mNativePtr, val);
    }

    public final void setDataPosition(int pos) {
        nativeSetDataPosition(mNativePtr, pos);
    }

    public final int readInt() {
        return nativeReadInt(mNativePtr);
    }

    private native long nativeCreate(); // 在native层构建 Parcel.cpp 对象指针地址
    private native void nativeWriteInt(long mNativePtr, int val); // 写入int值
    private native void nativeSetDataPosition(long mNativePtr, int pos); // 设置 mDataPos = 0  为了下载从0 的位置开始读
    private native int nativeReadInt(long mNativePtr); // 读取int值

}
