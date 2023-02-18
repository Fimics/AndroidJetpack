package com.hnradio.common.file;

import java.io.File;

public class FileFilter implements java.io.FileFilter
{
    private String[] mTypes;

    public FileFilter(String[] types) {
        this.mTypes = types;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        if (mTypes != null && mTypes.length > 0) {
            for (String mType : mTypes)
            {
                if (file.getName().endsWith(mType.toLowerCase()) || file.getName().endsWith(mType.toUpperCase()))
                {
                    return true;
                }
            }
        }else {
            return true;
        }
        return false;
    }
}
