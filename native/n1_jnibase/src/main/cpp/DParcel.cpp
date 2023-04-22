#include "DParcel.h"

DParcel::DParcel() {
   this->mData = static_cast<char *>(malloc(1024)); // 堆区
}

DParcel::~DParcel() {
    if (this->mData) {
        free(this->mData);
    }

    if (this->mDataPos) {
        this->mDataPos = NULL;
    }
}

void DParcel:: writeInt(int val) {
    // mData 挪动位置后 在给人家 = 50   第一次位置0   第二次4    第三次8
    *reinterpret_cast<int *>(this->mData + this->mDataPos) = val;
    changePos(sizeof(int ));
}

void DParcel::changePos(int pos) {
    this->mDataPos += pos;
}

void DParcel:: setDataPosition(int mDataPos) { // 用于改变指针地址挪动的位置
    this->mDataPos = mDataPos;
}

jint DParcel:: readInt() {
    jint ret = *reinterpret_cast<int *>(this->mData + this->mDataPos);
    changePos(sizeof(int ));
    return ret;
}
