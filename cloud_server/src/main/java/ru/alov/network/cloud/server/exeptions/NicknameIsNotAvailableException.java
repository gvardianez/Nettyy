package ru.alov.network.cloud.server.exeptions;

public class NicknameIsNotAvailableException extends RuntimeException {
    public NicknameIsNotAvailableException(String message) {
        super(message);
    }
}
