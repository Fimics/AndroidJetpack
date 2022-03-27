package com.mic.server.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface IHTTPSession {

        void execute() throws IOException;

        CookieHandler getCookies();

        Map<String, String> getHeaders();

        InputStream getInputStream();

        Method getMethod();

        Map<String, String> getParms();

        String getQueryParameterString();

        String getUri();

        void parseBody(Map<String, String> files) throws IOException, ResponseException;
    }