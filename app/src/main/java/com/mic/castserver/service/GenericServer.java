package com.mic.castserver.service;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;


class GenericServer implements IServer {

    private int tcpPort;
    private ServerSocket serverSocket;
    private Thread thread;
    private boolean isThreadRunning;

    @Override
    public void init(int port) throws IOException {
        tcpPort = -1;
        //try less then 10.
        for (int i = 0; i < 10; i++) {
            try {
                serverSocket = new ServerSocket(port - i * 2000);
                tcpPort = port - i * 2000;
                if (serverSocket.isClosed()) break;
            } catch (BindException e) {
                e.printStackTrace();
            }
        }

        thread = new Thread(new Runnable() {
            public void run() {
                isThreadRunning = true;
                try {
                    while (true) {
                        if(serverSocket!=null && !serverSocket.isClosed()){
                            Socket socket = serverSocket.accept();
                            new HttpService(socket);
                        }
                    }
                } catch (Exception ioe) {
                    ioe.printStackTrace();
                    isThreadRunning=false;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public final boolean wasStarted() {
        return this.serverSocket != null && !serverSocket.isClosed()
                && this.thread != null && isThreadRunning;
    }

    @Override
    public void stopAsync() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            serverSocket.close();
            thread.join();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int port() {
        return tcpPort;
    }

    @Override
    public boolean isClosed() {
        return serverSocket == null || serverSocket.isClosed();
    }

    @Override
    public void destroy() {
    }
}
