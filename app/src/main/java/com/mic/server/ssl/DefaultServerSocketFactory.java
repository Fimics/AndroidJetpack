package com.mic.server.ssl;

import java.io.IOException;
import java.net.ServerSocket;

public class DefaultServerSocketFactory implements ServerSocketFactory {

    @Override
    public ServerSocket create() throws IOException {
        return new ServerSocket();
    }
}
