package com.mic.jnibase;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ParcelBean implements Parcelable {

    protected ParcelBean(Parcel in) {
        in.readInt(); // 顺序不能乱
        in.readString();
    }

    public static final Creator<ParcelBean> CREATOR = new Creator<ParcelBean>() {
        @Override
        public ParcelBean createFromParcel(Parcel in) {
            return new ParcelBean(in);
        }

        @Override
        public ParcelBean[] newArray(int size) {
            return new ParcelBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(10); // 顺序不能乱
        dest.writeString("AAA");

    }
}
