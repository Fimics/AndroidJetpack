package com.mic.server.client;

import android.util.Log;

import com.mic.server.tempfile.TempFileManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunnable implements Runnable {

    private static final String TAG = "server";
    private final int timeout;
    public IOException bindException;
    public boolean hasBinded = false;

    private  String hostname;
    private  int myPort;
    private volatile ServerSocket myServerSocket;
    protected AsyncRunner asyncRunner;
    private TempFileManagerFactory tempFileManagerFactory;


    private ServerRunnable(int timeout,String hostname,int port,ServerSocket serverSocket,AsyncRunner asyncRunner,TempFileManagerFactory tempFileManagerFactory) {
        this.timeout = timeout;
        this.hostname=hostname;
        this.myPort=port;
        this.myServerSocket=serverSocket;
        this.asyncRunner=asyncRunner;
        this.tempFileManagerFactory=tempFileManagerFactory;
    }

    @Override
    public void run() {
        try {
            myServerSocket.bind(hostname != null ? new InetSocketAddress(hostname, myPort) : new InetSocketAddress(myPort));
            hasBinded = true;
        } catch (IOException e) {
            this.bindException = e;
            return;
        }
        do {
            try {
                final Socket finalAccept = myServerSocket.accept();
                if (this.timeout > 0) {
                    finalAccept.setSoTimeout(this.timeout);
                }
                final InputStream inputStream = finalAccept.getInputStream();
                asyncRunner.exec(ClientHandler.newInstance(inputStream, finalAccept, tempFileManagerFactory, asyncRunner));
            } catch (IOException e) {
                Log.d(TAG, "Communication with the client broken" + e.getMessage());
            }
        } while (!myServerSocket.isClosed());
    }

    public static ServerRunnable newInstance(int timeout,String hostname,int port,ServerSocket serverSocket,AsyncRunner asyncRunner,TempFileManagerFactory tempFileManagerFactory) {
        return new ServerRunnable(timeout,hostname,port,serverSocket,asyncRunner,tempFileManagerFactory);
    }

}