package ru.alov.network.cloud.common.models;

public class AuthOkMessage implements AbstractMessage{

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_OK;
    }
}
