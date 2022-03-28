package com.mic.castserver;

@SuppressWarnings("unused")
public class StatusCode {

    public static final String
            MIME_PLAIN_TEXT = "text/plain",
            MIME_DEFAULT_BINARY = "application/octet-stream";


    public static final String
            HTTP_OK = "200 OK",
            HTTP_PARTIAL_CONTENT = "206 Partial Content",
            HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable",
            HTTP_FORBIDDEN = "403 Forbidden",
            HTTP_NOT_FOUND = "404 Not Found",
            HTTP_BAD_REQUEST = "400 Bad HttpRequest",
            HTTP_INTERNAL_ERROR = "500 Internal Server Error";
}
