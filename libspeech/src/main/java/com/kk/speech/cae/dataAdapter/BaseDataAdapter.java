package com.kk.speech.cae.dataAdapter;

public abstract class BaseDataAdapter {
    protected static byte[] outData;

    public abstract byte[] adapter(byte[] source);
}
