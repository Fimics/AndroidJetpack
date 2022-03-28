package com.mic.castserver.service;

import java.io.IOException;

public interface IServer {

    void init(int port) throws IOException;

    void stopAsync();

    void stop();

    int port();

    boolean isClosed();

    void destroy();
}
