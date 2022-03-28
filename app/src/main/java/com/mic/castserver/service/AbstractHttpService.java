package com.mic.castserver.service;

import com.mic.castserver.ServerException;
import com.mic.castserver.request.HttpRequest;
import com.mic.castserver.request.IRequest;
import com.mic.castserver.response.HttpResponse;
import com.mic.castserver.response.IResponse;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

abstract class AbstractHttpService implements Runnable {

    private Socket socket;

    AbstractHttpService(Socket s) {
        if (s != null) {
            socket = s;
            Thread t = new Thread(this);
            t.setPriority(Thread.MAX_PRIORITY);
            t.setDaemon(true);
            t.start();
        }
    }

    public void run() {
        HttpResponse httpResponse;
        HttpRequest httpRequest;

        try {
            httpRequest = new HttpRequest(socket);
            Properties headers = httpRequest.headers();
            String url = httpRequest.url();
            httpResponse = new HttpResponse(socket, headers, url);
            service(httpRequest, httpResponse);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void service(IRequest httpRequest, IResponse httpResponse) throws InterruptedException, ServerException, IOException;
}
