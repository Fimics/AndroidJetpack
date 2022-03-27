package com.mic.server.http;

import java.io.IOException;
import java.net.ServerSocket;

public interface ServerSocketFactory {
    public ServerSocket create() throws IOException;
}