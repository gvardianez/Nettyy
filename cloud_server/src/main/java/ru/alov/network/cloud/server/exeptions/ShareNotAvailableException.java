package ru.alov.network.cloud.server.exeptions;

public class ShareNotAvailableException extends RuntimeException {
    public ShareNotAvailableException(String message){
        super(message);
    }
}
