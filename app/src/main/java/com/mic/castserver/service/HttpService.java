package com.mic.castserver.service;


import com.mic.castserver.ServerException;
import com.mic.castserver.request.IRequest;
import com.mic.castserver.response.IResponse;

import java.io.IOException;
import java.net.Socket;

class HttpService  extends AbstractHttpService {

    HttpService(Socket socket) {
        super(socket);
    }

    @Override
    protected void service(IRequest httpRequest, IResponse httpResponse) throws InterruptedException, ServerException, IOException {
        if(httpRequest!=null){
            httpRequest.info();
        }

        if(httpResponse!=null){
            httpResponse.process();
        }
    }
}
