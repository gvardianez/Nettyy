package ru.alov.network.cloud.common.file_transfer.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProtocolFileReceiver {

    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    public static final int progressByteSize = 65536*8;

    private String currentDir;
    private State currentState;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    private CommandSender commandSender;

    public void setCommandSender(CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    public ProtocolFileReceiver() {
        currentState = State.IDLE;
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public void startReceive() {
        currentState = State.NAME_LENGTH;
        receivedFileLength = 0L;
    }

    public void receive(ChannelHandlerContext ctx, ByteBuf buf, Runnable finishOperation) throws Exception {
        if (currentState == State.NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                nextLength = buf.readInt();
                currentState = State.NAME;
            }
        }

        if (currentState == State.NAME) {
            if (buf.readableBytes() >= nextLength) {
                byte[] fileName = new byte[nextLength];
                buf.readBytes(fileName);
                String file = new String(fileName, StandardCharsets.UTF_8);
                out = new BufferedOutputStream(new FileOutputStream(String.valueOf(Paths.get(currentDir, file))));
                currentState = State.FILE_LENGTH;
                System.out.println(file);
            }
        }

        if (currentState == State.FILE_LENGTH) {
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                currentState = State.FILE;
            }
        }

        if (currentState == State.FILE) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (commandSender != null && receivedFileLength % progressByteSize == 0) {
                    commandSender.sendFilePartCommand();
                }
                if (fileLength == receivedFileLength) {
                    System.out.println("Принято");
                    currentState = State.IDLE;
                    out.close();
                    finishOperation.run();
                    return;
                }
            }
        }
    }
}
