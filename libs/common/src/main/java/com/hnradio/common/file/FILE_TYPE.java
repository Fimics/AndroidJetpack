package com.hnradio.common.file;

/**
 * Created by ytf on 2020/04/13.
 * Description:
 */
public enum FILE_TYPE
{
    TYPE_JPEG("JPEG"), TYPE_PNG("PNG"),TYPE_DOC("DOC"),TYPE_PDF("PDF"), TYPE_DIR("DIR"), TYPE_FILE("FILE");

    String name;

    public String getName()
    {
        return name;
    }

    FILE_TYPE(String name)
    {
        this.name = name;
    }
}
