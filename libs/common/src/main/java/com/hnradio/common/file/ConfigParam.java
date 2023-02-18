package com.hnradio.common.file;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigParam implements Parcelable
{

    public boolean mutilyMode;
    public String[] fileTypes;
    public String path;

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeByte(this.mutilyMode ? (byte) 1 : (byte) 0);
        dest.writeStringArray(this.fileTypes);
        dest.writeString(this.path);
    }

    public ConfigParam()
    {
    }

    protected ConfigParam(Parcel in)
    {
        this.mutilyMode = in.readByte() != 0;
        this.fileTypes = in.createStringArray();
        this.path = in.readString();
    }

    public static final Creator<ConfigParam> CREATOR = new Creator<ConfigParam>()
    {
        @Override
        public ConfigParam createFromParcel(Parcel source)
        {
            return new ConfigParam(source);
        }

        @Override
        public ConfigParam[] newArray(int size)
        {
            return new ConfigParam[size];
        }
    };
}
