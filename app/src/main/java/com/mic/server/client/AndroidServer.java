package com.mic.server.client;

import com.mic.server.http.NanoHTTPD;
public class AndroidServer extends NanoHTTPD {

    public AndroidServer(int port) {
        super(port);
    }

    public AndroidServer(String hostname, int port) {
        super(hostname, port);
    }
}
