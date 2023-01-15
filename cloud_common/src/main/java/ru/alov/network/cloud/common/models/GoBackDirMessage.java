package ru.alov.network.cloud.common.models;

public class GoBackDirMessage implements AbstractMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.GO_BACK_DIR;
    }
}
