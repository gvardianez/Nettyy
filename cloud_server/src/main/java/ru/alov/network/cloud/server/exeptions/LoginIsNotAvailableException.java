package ru.alov.network.cloud.server.exeptions;

public class LoginIsNotAvailableException extends RuntimeException {
    public LoginIsNotAvailableException(String message) {
        super(message);
    }
}
