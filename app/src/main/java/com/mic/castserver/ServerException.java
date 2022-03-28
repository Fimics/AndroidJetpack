
package com.mic.castserver;

public class ServerException extends Exception {

    private static final long serialVersionUID = 1L;

    public ServerException() {
        super();
    }

    public ServerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ServerException(String detailMessage) {
        super(detailMessage);
    }

    public ServerException(Throwable throwable) {
        super(throwable);
    }

}
