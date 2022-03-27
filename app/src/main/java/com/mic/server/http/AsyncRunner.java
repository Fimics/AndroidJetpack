package com.mic.server.http;

public interface AsyncRunner {

        void closeAll();

        void closed(ClientHandler clientHandler);

        void exec(ClientHandler code);
    }