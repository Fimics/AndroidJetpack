package com.mic.castserver.response;


import com.mic.castserver.ServerException;

import java.io.IOException;
import java.util.Properties;

@SuppressWarnings("unused")
public interface IResponse {

    void process() throws ServerException, IOException, InterruptedException;

    Properties requestHeaders();

    String requestUrl();
}
