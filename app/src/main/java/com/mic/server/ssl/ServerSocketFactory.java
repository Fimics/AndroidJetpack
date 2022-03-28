package com.mic.server.ssl;

import java.io.IOException;
import java.net.ServerSocket;

public interface ServerSocketFactory {
    public ServerSocket create() throws IOException;
}