package ru.alov.network.cloud.client;

import io.netty.channel.Channel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Data;
import ru.alov.network.cloud.client.network.Network;
import ru.alov.network.cloud.common.file_handlers.FileDeleter;
import ru.alov.network.cloud.common.file_transfer.FileUploader;
import ru.alov.network.cloud.common.file_transfer.protocol.CommandSender;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolFileReceiver;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolFileSender;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolHandler;
import ru.alov.network.cloud.common.models.*;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Data
public class Controller implements Initializable {

    public static Controller controller;

    public ProgressBar progressBarForClient;
    public VBox serverBox;
    public ProgressBar progressBarForServer;
    private FileUploader fileUploader;
    public VBox sharePanel;
    public TextField shareField;
    public VBox createDirOnClient;
    public TextField createDirTextField;
    public VBox createDirOnServer;
    public TextField createDirOnServerTextField;
    public ListView<String> clientFiles;
    public ListView<String> serverFiles;
    public TextField loginField;
    public PasswordField passwordField;
    public TextField loginFieldReg;
    public PasswordField passwordFieldReg;
    public TextField nickFieldReg;
    public VBox loginPanel;
    public VBox registrationPanel;
    public HBox cloudPanel;
    private Path currentDir;
    private Channel channel;
    private ProtocolHandler protocolHandler;
    private ProtocolFileSender protocolFileSender;
    private CommandSender commandSender;
    private double progressBarValue;
    private long fileSize;

    public static Controller getInstance() {
        return controller;
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public void fillServerView(List<String> list) {
        serverFiles.getItems().clear();
        serverFiles.getItems().addAll(list);
    }

    public void fillClientView(List<String> list) {
        clientFiles.getItems().clear();
        clientFiles.getItems().addAll(list);
    }

    public List<String> getFileNames() {
        try {
            return Files.list(currentDir)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            currentDir = Paths.get("D:\\");
//            currentDir = Paths.get(System.getProperty("user.home"));
            setPropertiesClientFiles();
            setPropertiesServerFiles();
            CountDownLatch connectionOpened = new CountDownLatch(1);
            controller = this;
            new Thread(() -> Network.getInstance().start(connectionOpened)).start();
            connectionOpened.await();
            channel = Network.getCurrentChannel();
            this.fileUploader = new FileUploader(channel, ApplicationProperties.BUFF_SIZE, progressBarForClient);
            this.protocolHandler = Network.getProtoHandler();
            this.commandSender = new CommandSender(channel);
            this.protocolFileSender = new ProtocolFileSender(commandSender);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setPropertiesClientFiles() {
        clientFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        clientFiles.getItems().addAll(getFileNames());
        clientFiles.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String file = clientFiles.getSelectionModel().getSelectedItem();
                Path path = currentDir.resolve(file);
                if (Files.isDirectory(path)) {
                    currentDir = path;
                    protocolHandler.getFileReceiver().setCurrentDir(currentDir.toString());
                    fillClientView(getFileNames());
                } else {
                    try {
                        Desktop.getDesktop().open(path.toFile());
                    } catch (IOException ioException) {
                        showError(ioException.getMessage());
                        ioException.printStackTrace();
                    }
                }
            }
        });
    }

    private void setPropertiesServerFiles() {
        serverFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverFiles.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                commandSender.sendOpenDirectoryCommand(serverFiles.getSelectionModel().getSelectedItem());
            }
        });
    }

    public void upload(ActionEvent actionEvent) {
        commandSender.sendFilePackageRequestCommand();
    }

    public void uploadOnServer() {
        List<String> selectedFiles = new ArrayList<>(clientFiles.getSelectionModel().getSelectedItems());
        if (selectedFiles.size() == 0) return;
        try {
            fileSize = fileSize + protocolFileSender.calculateFilesSize(selectedFiles, currentDir);
            System.out.println(fileSize);
            protocolFileSender.sendFile(selectedFiles, currentDir, channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setProgressbarPart() {
        progressBarValue = progressBarValue + ProtocolFileReceiver.progressByteSize / (double) fileSize;
        progressBarForClient.setProgress(progressBarValue);
//        System.out.println(progressBarValue);
        if (progressBarValue > 0.99) {
            System.out.println(progressBarValue);
            fileSize = 0;
            progressBarValue = 0;
            progressBarForClient.setProgress(progressBarValue);
        }
    }


    public void downLoad(ActionEvent actionEvent) throws IOException {
        List<String> selectedFiles = new ArrayList<>(serverFiles.getSelectionModel().getSelectedItems());
        if (selectedFiles.size() == 0) return;
        commandSender.sendFilesRequestCommand(selectedFiles);
    }

    public void registrationAuth(ActionEvent actionEvent) throws IOException {
        if (loginFieldReg.getText().trim().isEmpty() || nickFieldReg.getText().trim().isEmpty() || passwordFieldReg.getText().trim().isEmpty())
            return;
        channel.writeAndFlush(new RegistrationAuth(loginFieldReg.getText(), nickFieldReg.getText(), passwordFieldReg.getText()));
    }

    public void sendAuth(ActionEvent actionEvent) {
        if (loginField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) return;
        commandSender.sendAuthCommand(loginField.getText(), passwordField.getText());
    }

    public void registration(ActionEvent actionEvent) {
        loginPanel.setVisible(false);
        registrationPanel.setVisible(true);
    }

    public void goBackOnLoginPanel(ActionEvent actionEvent) {
        loginPanel.setVisible(true);
        registrationPanel.setVisible(false);
    }

    public void deleteOnClient(ActionEvent actionEvent) {
        List<String> selectedFiles = new ArrayList<>(clientFiles.getSelectionModel().getSelectedItems());
        if (selectedFiles.size() == 0) return;
        FileDeleter.deleteFiles(selectedFiles, currentDir);
        Platform.runLater(() -> fillClientView(getFileNames()));
    }

    public void deleteFromServer(ActionEvent actionEvent) {
        List<String> selectedFiles = new ArrayList<>(serverFiles.getSelectionModel().getSelectedItems());
        if (selectedFiles.size() == 0) return;
        commandSender.sendFilesDeleteCommand(selectedFiles);
    }

    public void back(ActionEvent actionEvent) {
        if (currentDir.toString().length() != 3) {
            currentDir = currentDir.getParent();
            protocolHandler.getFileReceiver().setCurrentDir(currentDir.toString());
        }
        Platform.runLater(() -> fillClientView(getFileNames()));
    }

    public void refresh(ActionEvent actionEvent) {
        Platform.runLater(() -> fillClientView(getFileNames()));
    }

    public void backOnServer(ActionEvent actionEvent) {
        commandSender.sendGoBackDirectoryCommand();
    }

    public void shareFile(ActionEvent actionEvent) {
        if (serverFiles.getSelectionModel().getSelectedItems().isEmpty()) return;
        cloudPanel.setVisible(false);
        sharePanel.setVisible(true);
    }

    public void shareFileRequest(ActionEvent actionEvent) {
        if (shareField.getText().trim().isEmpty()) return;
        sharePanel.setVisible(false);
        cloudPanel.setVisible(true);
        commandSender.sendShareFileCommand(serverFiles.getSelectionModel().getSelectedItems(), shareField.getText());
    }

    public void createDirectory(ActionEvent actionEvent) {
        cloudPanel.setVisible(false);
        createDirOnClient.setVisible(true);
    }

    public void createDirOnClient(ActionEvent actionEvent) {
        cloudPanel.setVisible(true);
        createDirOnClient.setVisible(false);
        createDirectory(createDirTextField.getText());
    }

    public void createDirectory(String fileName) {
        try {
            Files.createDirectory(currentDir.resolve(fileName));
        } catch (FileAlreadyExistsException | InvalidPathException e) {
            Platform.runLater(() -> showError(e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createDirOnServer(ActionEvent actionEvent) {
        cloudPanel.setVisible(true);
        createDirOnServer.setVisible(false);
        commandSender.sendCreateDirectoryCommand(createDirOnServerTextField.getText());
    }

    public void createDirectoryOnServer(ActionEvent actionEvent) {
        cloudPanel.setVisible(false);
        createDirOnServer.setVisible(true);
    }

    public void refreshOnServer(ActionEvent actionEvent) {
        System.out.println("refresh");
       commandSender.sendRefreshCommand();
    }

}