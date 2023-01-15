package ru.alov.network.cloud.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import ru.alov.network.cloud.client.Controller;
import ru.alov.network.cloud.common.file_transfer.FileDownloader;
import ru.alov.network.cloud.common.models.*;

@Slf4j
public class AbstractMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final Controller controller;

    public AbstractMessageHandler() {
        controller = Controller.getInstance();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                AbstractMessage message) {
        System.out.println(Thread.currentThread().getName());
        switch (message.getMessageType()) {
            case ERROR:
                ErrorMessage errorMessage = (ErrorMessage) message;
                Platform.runLater(() -> controller.showError(errorMessage.getErrorMessage()));
                break;
            case AUTH_OK:
                controller.getLoginPanel().setVisible(false);
                controller.getCloudPanel().setVisible(true);
                break;
            case FILES_LIST:
                FilesList files = (FilesList) message;
                Platform.runLater(() -> controller.fillServerView(files.getFiles()));
                break;
            case RECEIVE_SHARE_FILES:
                ListShareFilesMessage listShareFilesMessage = (ListShareFilesMessage) message;
                Platform.runLater(() -> controller.fillServerView(listShareFilesMessage.getShareFiles()));
                break;
            case CREATE_DIR:
                CreateDirectoryMessage createDirectoryMessage = (CreateDirectoryMessage) message;
                controller.createDirectory(createDirectoryMessage.getName());
                Platform.runLater(() -> controller.fillClientView(controller.getFileNames()));
                break;
            case FILE:
                FileMessage fileMessage = (FileMessage) message;
                if (fileMessage.getProgress() >= 1) {
                    Platform.runLater(() -> controller.progressBarForServer.setProgress(0));
                    Platform.runLater(() -> controller.fillClientView(controller.getFileNames()));
                } else {
                    Platform.runLater(() -> controller.progressBarForServer.setProgress(fileMessage.getProgress()));
                }
                FileDownloader.downloadFile(fileMessage, controller.getCurrentDir());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
//        Network.stop();
        log.debug("Client disconnected");
        ctx.close();
    }

}
