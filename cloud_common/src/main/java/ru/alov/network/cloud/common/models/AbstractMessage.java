package ru.alov.network.cloud.common.models;


import java.io.Serializable;

public interface AbstractMessage extends Serializable {

    MessageType getMessageType();

}