package ru.alov.network.cloud.common.models;

import lombok.Data;

@Data
public class ErrorMessage implements AbstractMessage{

   private String errorMessage;

    public ErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ERROR;
    }
}
