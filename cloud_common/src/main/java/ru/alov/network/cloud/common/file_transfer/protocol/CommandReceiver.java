package ru.alov.network.cloud.common.file_transfer.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public abstract class CommandReceiver {

    public enum State {
        IDLE, COMMAND_TYPE, COMMAND_BODY_LENGTH, COMMAND_BODY
    }

    private State currentState;
    private byte commandType;
    private int bodyLength;

    public CommandReceiver() {
        currentState = State.IDLE;
    }

    public void startReceive() {
        currentState = State.COMMAND_TYPE;
    }

    public void receive(ChannelHandlerContext ctx, ByteBuf buf, Runnable finishOperation) {
        if (currentState == State.COMMAND_TYPE) {
            if (buf.readableBytes() >= 1) {
                commandType = buf.readByte();
                currentState = State.COMMAND_BODY_LENGTH;
            }
        }

        if (currentState == State.COMMAND_BODY_LENGTH) {
            if (buf.readableBytes() >= 4) {
                bodyLength = buf.readInt();
                currentState = State.COMMAND_BODY;
            }
        }
        try {
            if (currentState == State.COMMAND_BODY) {
                if (bodyLength == 0) {
                    parseCommand(ctx, commandType, "");
                    currentState = State.IDLE;
                    finishOperation.run();
                    return;
                }
                if (buf.readableBytes() >= bodyLength) {
                    byte[] commandBody = new byte[bodyLength];
                    buf.readBytes(commandBody);
                    String command = new String(commandBody, StandardCharsets.UTF_8);
                    parseCommand(ctx, commandType, command);
                    currentState = State.IDLE;
                    finishOperation.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void parseCommand(ChannelHandlerContext ctx, byte commandType, String command) throws Exception;
}
