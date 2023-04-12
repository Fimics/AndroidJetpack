#ifndef DERRYPARCEL_JAVA_DPARCEL_H
#define DERRYPARCEL_JAVA_DPARCEL_H

#include <jni.h>
#include <malloc.h>

class DParcel {

private:
    char * mData = 0; // DParcel对象共享内存的首地址（内存地址）
    int mDataPos = 0; // DParcel对象共享内存的首地址（内存地址） 的指针挪动位置
    void changePos(int pos); // 指针位置 +=
    jint len = 0;

public:
    DParcel();

    virtual ~DParcel();

    void writeInt(int val);

    void setDataPosition(int mDataPos); // 用于改变指针地址挪动的位置

    jint readInt();
};


#endif //DERRYPARCEL_JAVA_DPARCEL_H
