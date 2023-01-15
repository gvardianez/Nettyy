package ru.alov.network.cloud.common.models;

import lombok.Data;

@Data
public class CreateDirectoryMessage implements AbstractMessage{

    private String name;

    public CreateDirectoryMessage(String path){
        this.name = path;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATE_DIR;
    }
}
