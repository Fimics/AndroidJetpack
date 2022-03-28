package com.mic.server;


public class Constant {
    public static final int SOCKET_READ_TIMEOUT = 500000;
    public static final String MIME_PLAINTEXT = "text/plain";
        public static final String MIME_JSON = "application/json";
    //这个参数的意思是以流的形式下载文件，这样可以实现任意格式的文件下载
//    public static final String MIME_JSON = "application/octet-stream";
    public static final String MIME_HTML = "text/html";
    public static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";

}
