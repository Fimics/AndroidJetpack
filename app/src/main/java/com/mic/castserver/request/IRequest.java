package com.mic.castserver.request;


import com.mic.castserver.ServerException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public interface IRequest {

    void process() throws IOException, ServerException, ServerException;

    int port();

    InetAddress address();

    String method();

    String url();

    Properties headers();

    void info();
}
