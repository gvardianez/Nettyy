package ru.alov.network.cloud.common.file_transfer.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CommandSender {

    private final static char delimiter = 10000;

    private final Channel channel;

    public CommandSender(Channel channel) {
        this.channel = channel;
    }

    public void sendAuthCommand(String login, String password) {
        String command = login + delimiter + password;
        byte[] commandBody = command.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = getByteBuf(commandBody);

        sendByteBuf(commandBody, buf, CommandType.AUTH);
    }

    public void sendAuthOkCommand() {
        ByteBuf buf = getByteBuf();
        sendByteBuf(buf, CommandType.AUTH_OK);
    }

    public void sendFilesListCommand(List<String> files) {
        if (files.isEmpty()) {

            ByteBuf buf = getByteBuf();

            sendByteBuf(buf, CommandType.FILES_LIST);
        } else {
            byte[] commandBody = getCommandBody(files);

            ByteBuf buf = getByteBuf(commandBody);

            sendByteBuf(commandBody, buf, CommandType.FILES_LIST);
        }
    }

    public void sendRefreshCommand() {
        ByteBuf buf = getByteBuf();
        sendByteBuf(buf, CommandType.REFRESH);
    }

    public void sendFilesDeleteCommand(List<String> files) {
        byte[] commandBody = getCommandBody(files);

        ByteBuf buf = getByteBuf(commandBody);

        sendByteBuf(commandBody, buf, CommandType.FILES_DELETE);
    }

    public void sendFilesRequestCommand(List<String> files) {
        byte[] commandBody = getCommandBody(files);

        ByteBuf buf = getByteBuf(commandBody);

        sendByteBuf(commandBody, buf, CommandType.FILES_REQUEST);
    }

    public void sendCreateDirectoryCommand(String directoryName) {
        byte[] commandBody = directoryName.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = getByteBuf(commandBody);

        sendByteBuf(commandBody, buf, CommandType.CREATE_DIRECTORY);
    }

    public void sendErrorCommand(String error) {
        byte[] commandBody = error.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = getByteBuf(commandBody);

        sendByteBuf(commandBody, buf, CommandType.ERROR);
    }

    public void sendOpenDirectoryCommand(String directory) {
        byte[] commandBody = directory.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = getByteBuf(commandBody);

        sendByteBuf(commandBody, buf, CommandType.OPEN_DIRECTORY);
    }

    public void sendGoBackDirectoryCommand() {
        ByteBuf buf = getByteBuf();

        sendByteBuf(buf, CommandType.GO_BACK_DIRECTORY);
    }

    public void sendShareFileCommand(List<String> files, String nickname) {
        StringBuilder stringBuilder = new StringBuilder();
        files.forEach(s -> stringBuilder.append(s).append(delimiter));
        stringBuilder.append(nickname);

        byte[] commandBody = stringBuilder.toString().getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = getByteBuf(commandBody);

        sendByteBuf(commandBody, buf, CommandType.SHARE_FILE);
    }

    public void sendFilePackageRequestCommand() {
        ByteBuf buf = getByteBuf();

        sendByteBuf(buf, CommandType.FILE_PACKAGE_REQUEST);
    }

    public void sendFilePackageResponseCommand(String response) {
        byte[] commandBody = response.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = getByteBuf(commandBody);

        sendByteBuf(commandBody, buf, CommandType.FILE_PACKAGE_RESPONSE);
    }

    public void sendFilePartCommand() {
        ByteBuf buf = getByteBuf();

        sendByteBuf(buf, CommandType.FILE_PART);
    }

    private void sendByteBuf(ByteBuf buf, CommandType commandType) {
        buf.writeByte(NetworkCloudCommandsList.CMD_SIGNAL_BYTE);
        buf.writeByte(commandType.getByte());
        buf.writeInt(0);
        channel.writeAndFlush(buf);
    }

    private void sendByteBuf(byte[] commandBody, ByteBuf buf, CommandType commandType) {
        buf.writeByte(NetworkCloudCommandsList.CMD_SIGNAL_BYTE);
        buf.writeByte(commandType.getByte());
        buf.writeInt(commandBody.length);
        buf.writeBytes(commandBody);
        channel.writeAndFlush(buf);
    }

    private byte[] getCommandBody(List<String> files) {
        StringBuilder stringBuilder = new StringBuilder();
        files.forEach(s -> stringBuilder.append(s).append(delimiter));
        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ByteBuf getByteBuf() {
        return ByteBufAllocator.DEFAULT.directBuffer(1 + 1 + 4);
    }

    private ByteBuf getByteBuf(byte[] commandBody) {
        return ByteBufAllocator.DEFAULT.directBuffer(1 + 1 + 4 + commandBody.length);
    }

}
