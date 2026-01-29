package com.mic.server.client;

public interface AsyncRunner {

        void closeAll();

        void closed(ClientHandler clientHandler);

        void exec(ClientHandler code);
    }