package com.mic.server.client;

import android.util.Log;

import com.mic.server.request.HTTPSession;
import com.mic.server.Utils;
import com.mic.server.tempfile.TempFileManager;
import com.mic.server.tempfile.TempFileManagerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ClientHandler implements Runnable {

    private static final String TAG = "ClientHandler";
    private final InputStream inputStream;

    private final Socket acceptSocket;

    private TempFileManagerFactory tempFileManagerFactory;

    protected AsyncRunner asyncRunner;

    private ClientHandler(InputStream inputStream, Socket acceptSocket, TempFileManagerFactory tempFileManagerFactory,AsyncRunner asyncRunner) {
        this.inputStream = inputStream;
        this.acceptSocket = acceptSocket;
        this.tempFileManagerFactory = tempFileManagerFactory;
        this.asyncRunner=asyncRunner;
    }

    public static ClientHandler newInstance(InputStream inputStream, Socket acceptSocket, TempFileManagerFactory tempFileManagerFactory,AsyncRunner asyncRunner) {
        return new ClientHandler(inputStream, acceptSocket,tempFileManagerFactory,asyncRunner);
    }


    public void close() {
        Utils.safeClose(this.inputStream);
        Utils.safeClose(this.acceptSocket);
    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        try {
            outputStream = this.acceptSocket.getOutputStream();
            TempFileManager tempFileManager = tempFileManagerFactory.create();
            HTTPSession session = new HTTPSession(tempFileManager, this.inputStream, outputStream, this.acceptSocket.getInetAddress());
            while (!this.acceptSocket.isClosed()) {
                session.execute();
            }
        } catch (Exception e) {
            // When the socket is closed by the client,
            // we throw our own SocketException
            // to break the "keep alive" loop above. If
            // the exception was anything other
            // than the expected SocketException OR a
            // SocketTimeoutException, print the
            // stacktrace
            if (!(e instanceof SocketException && "NanoHttpd Shutdown".equals(e.getMessage())) && !(e instanceof SocketTimeoutException)) {
                Log.d(TAG, "Communication with the client broken", e);
            }
        } finally {
            Utils.safeClose(outputStream);
            Utils.safeClose(this.inputStream);
            Utils.safeClose(this.acceptSocket);
            asyncRunner.closed(this);
        }
    }
}