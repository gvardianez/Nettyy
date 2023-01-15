package ru.alov.network.cloud.common.models;

import lombok.Data;

@Data
public class OpenDirectoryMessage implements AbstractMessage {

    private String name;

    public OpenDirectoryMessage(String name){
        this.name = name;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.OPEN_DIR;
    }
}
