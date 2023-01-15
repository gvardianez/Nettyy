package ru.alov.network.cloud.server.exeptions;

public class AlreadyAuthorizeException extends RuntimeException {
    public AlreadyAuthorizeException(String message) {
        super(message);
    }
}
