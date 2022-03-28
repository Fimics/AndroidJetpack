package com.mic.castserver.service;


import java.io.IOException;

public class SimpleServer {

    private GenericServer genericServer;
    private int port;
    private static SimpleServer simpleServer;

    public static SimpleServer createServer(int port) {
        if (simpleServer == null) {
            simpleServer = new SimpleServer(port);
        }
        return simpleServer;
    }

    private SimpleServer(int port) {
        this.port = port;
        start();
    }

    public void start() {
        if (genericServer == null || genericServer.isClosed()) {
            genericServer = new GenericServer();
            try {
                genericServer.init(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopAsync() {
        if (genericServer != null && !genericServer.isClosed()) {
            genericServer.stopAsync();
            genericServer=null;
        }
    }

    public void stop() {
        if (genericServer != null && !genericServer.isClosed()) {
            genericServer.stop();
            genericServer=null;
        }
    }

    public int port() {
        return genericServer.port();
    }

    public static SimpleServer getSimpleServer() {
        return simpleServer;
    }

    public boolean isStarted(){
        return genericServer ==null? false:genericServer.wasStarted();
    }
}

