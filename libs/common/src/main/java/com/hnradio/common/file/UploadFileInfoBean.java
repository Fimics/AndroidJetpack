package com.hnradio.common.file;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ytf on 2020/04/13.
 * Description:
 */
public class UploadFileInfoBean implements Parcelable
{
    public String name;
    public String path;
    public long size;
    public FILE_TYPE type;
    public boolean isAdd;
    public boolean isPredeleting;

    public static UploadFileInfoBean addItem()
    {
        UploadFileInfoBean p = new UploadFileInfoBean();
        p.isAdd = true;
        return p;
    }

    public UploadFileInfoBean copy()
    {
        UploadFileInfoBean o = new UploadFileInfoBean();
        o.name = name;
        o.path = path;
        o.type = type;
        return o;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeLong(this.size);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeByte(this.isAdd ? (byte) 1 : (byte) 0);
    }

    public UploadFileInfoBean()
    {
    }

    protected UploadFileInfoBean(Parcel in)
    {
        this.name = in.readString();
        this.path = in.readString();
        this.size = in.readLong();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : FILE_TYPE.values()[tmpType];
        this.isAdd = in.readByte() != 0;
    }

    public static final Creator<UploadFileInfoBean> CREATOR = new Creator<UploadFileInfoBean>()
    {
        @Override
        public UploadFileInfoBean createFromParcel(Parcel source)
        {
            return new UploadFileInfoBean(source);
        }

        @Override
        public UploadFileInfoBean[] newArray(int size)
        {
            return new UploadFileInfoBean[size];
        }
    };
}
