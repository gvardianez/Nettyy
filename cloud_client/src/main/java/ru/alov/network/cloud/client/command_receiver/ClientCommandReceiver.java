package ru.alov.network.cloud.client.command_receiver;

import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import ru.alov.network.cloud.client.Controller;
import ru.alov.network.cloud.common.file_transfer.protocol.CommandReceiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ClientCommandReceiver extends CommandReceiver {

    private final Controller controller;

    private final char delimiter = 10000;

    public ClientCommandReceiver() {
        controller = Controller.getInstance();
    }

    @Override
    public void parseCommand(ChannelHandlerContext ctx, byte commandType, String command) {
        switch (commandType) {
            case 20:
                filesListServer(command);
                break;
            case 19:
                controller.getLoginPanel().setVisible(false);
                controller.getCloudPanel().setVisible(true);
                break;
            case 24:
                Platform.runLater(() -> controller.showError(command));
                break;
            case 23:
                controller.createDirectory(command);
                Platform.runLater(() -> controller.fillClientView(controller.getFileNames()));
                break;
            case 30:
                checkFilePackageResponseCommand(command);
                break;
            case 31:
                Platform.runLater(controller::setProgressbarPart);
                break;
        }

    }

    private void checkFilePackageResponseCommand(String command) {
        if (command.equals("OK")) Platform.runLater(controller::uploadOnServer);
        else  Platform.runLater(() -> controller.showError(command));
    }

    private void filesListServer(String command) {
        List<String> serverFilesList;
        if (command == null) {
            serverFilesList = Collections.emptyList();
        } else {
            serverFilesList = new ArrayList<>(Arrays.asList(command.split(String.valueOf(delimiter))));
        }
        Platform.runLater(() -> controller.fillServerView(serverFilesList));
    }

}
