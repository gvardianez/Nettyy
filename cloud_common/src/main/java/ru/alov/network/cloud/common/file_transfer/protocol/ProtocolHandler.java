package ru.alov.network.cloud.common.file_transfer.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {

    private enum Status {
        IDLE, FILE_RECEIVE, COMMAND
    }

    private final CommandReceiver commandReceiver;
    private final ProtocolFileReceiver protocolFileReceiver;
    private Status currentStatus;
    private final Runnable finishCallback;

    public ProtocolHandler(ProtocolFileReceiver protocolFileReceiver, CommandReceiver commandReceiver) {
        this.currentStatus = Status.IDLE;
        this.protocolFileReceiver = protocolFileReceiver;
        this.commandReceiver = commandReceiver;
        finishCallback = () -> currentStatus = Status.IDLE;
    }

    public ProtocolFileReceiver getFileReceiver() {
        return protocolFileReceiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)  {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentStatus == Status.IDLE) {
                byte controlByte = buf.readByte();
                if (controlByte == NetworkCloudCommandsList.FILE_SEND_SIGNAL_BYTE) {
                    System.out.println(controlByte);
                    currentStatus = Status.FILE_RECEIVE;
                    protocolFileReceiver.startReceive();
                } else if (controlByte == NetworkCloudCommandsList.CMD_SIGNAL_BYTE) {
                    System.out.println(controlByte);
                    currentStatus = Status.COMMAND;
                    commandReceiver.startReceive();
                }
            }
            try {
                if (currentStatus == Status.FILE_RECEIVE) {
                    protocolFileReceiver.receive(ctx, buf, finishCallback);
                }
                if (currentStatus == Status.COMMAND) {
                    commandReceiver.receive(ctx, buf, finishCallback);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
