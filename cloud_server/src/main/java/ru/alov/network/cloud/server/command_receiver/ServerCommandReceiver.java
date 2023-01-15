package ru.alov.network.cloud.server.command_receiver;

import io.netty.channel.ChannelHandlerContext;
import ru.alov.network.cloud.common.file_handlers.FileCopier;
import ru.alov.network.cloud.common.file_handlers.FileDeleter;
import ru.alov.network.cloud.common.file_transfer.protocol.CommandReceiver;
import ru.alov.network.cloud.common.file_transfer.protocol.CommandSender;
import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolFileReceiver;

import ru.alov.network.cloud.common.file_transfer.protocol.ProtocolFileSender;
import ru.alov.network.cloud.server.ApplicationProperties;
import ru.alov.network.cloud.server.exeptions.AlreadyAuthorizeException;
import ru.alov.network.cloud.server.exeptions.ShareNotAvailableException;
import ru.alov.network.cloud.server.exeptions.UserNotFoundException;
import ru.alov.network.cloud.server.exeptions.WrongCredentialsException;
import ru.alov.network.cloud.server.netty_server.NettyServer;
import ru.alov.network.cloud.server.services.database.AuthorizationService;
import ru.alov.network.cloud.server.services.database.DataBaseService;
import ru.alov.network.cloud.server.services.database.ShareService;

import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ServerCommandReceiver extends CommandReceiver {

    private final String shareDir = "SHARING";
    private final AuthorizationService dataBaseAuthService;
    private final ShareService shareService;
    private final ProtocolFileReceiver protocolFileReceiver;
    private final ProtocolFileSender protocolFileSender;
    private final FileCopier fileCopier;
    private final String basePath;
    private Path currentPath;
    private Path startSharePath;
    private Path sharePath;
    private String nickName;
    private boolean isShareDir;
    private boolean isFirstEnterShare;
    private CommandSender commandSender;

    private final char delimiter;

    public ServerCommandReceiver(ProtocolFileReceiver protocolFileReceiver) {
        this.dataBaseAuthService = DataBaseService.getAuthorizationService();
        this.shareService = DataBaseService.getShareService();
        this.protocolFileSender = new ProtocolFileSender(commandSender);
        this.fileCopier = new FileCopier();
        this.basePath = ApplicationProperties.FILES_BASE_PATH;
        this.currentPath = Paths.get(this.basePath);
        this.protocolFileReceiver = protocolFileReceiver;
        isFirstEnterShare = true;
        delimiter = 10000;
    }

    @Override
    public void parseCommand(ChannelHandlerContext ctx, byte commandType, String command) {
        try {
            checkSender(ctx);
            switch (commandType) {
                case 18:
                    System.out.println(commandType);
                    authorization(command);
                    break;
                case 21: {
                    System.out.println(commandType);
                    if (isShareDir) return;
                    commandSender.sendFilesListCommand(getFilesListFromPath(currentPath));
                    break;
                }
                case 22: {
                    System.out.println(commandType);
                    List<String> files = getFilesList(command);
                    files.remove(Paths.get(basePath, nickName, "SHARING").toString());
                    FileDeleter.deleteFiles(files, currentPath);
                    commandSender.sendFilesListCommand(getFilesListFromPath(currentPath));
                    break;
                }
                case 23: {
                    System.out.println(commandType);
                    if (checkShareDir(currentPath.toString())) return;
                    try {
                        Files.createDirectory(currentPath.resolve(command));
                    } catch (FileAlreadyExistsException e) {
                        commandSender.sendErrorCommand("Already exists " + e.getMessage());
                    }
                    commandSender.sendFilesListCommand(getFilesListFromPath(currentPath));
                    break;
                }
                case 25:
                    System.out.println(commandType);
                    openDirectory(command);
                    break;
                case 26:
                    System.out.println(commandType);
                    goBackDir(ctx);
                    break;
                case 27:
                    System.out.println(commandType);
                    List<String> files = getFilesList(command);
                    protocolFileSender.sendFile(files, currentPath, ctx.channel());
                    break;
                case 28:
                    System.out.println(commandType);
                    shareFile(ctx, command);
                    break;
                case 29:
                    System.out.println(commandType);
                    checkFilePackageRequest(ctx);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkSender(ChannelHandlerContext ctx) {
        if (commandSender == null) {
            commandSender = new CommandSender(ctx.channel());
            protocolFileReceiver.setCommandSender(commandSender);
        }
    }

    private void checkFilePackageRequest(ChannelHandlerContext ctx) {
        if (isShareDir)
            commandSender.sendFilePackageResponseCommand(
                    "Can't upload file to this directory");
        else {
            protocolFileReceiver.setCurrentDir(currentPath.toString());
            commandSender.sendFilePackageResponseCommand(
                    "OK");
        }

    }

    private List<String> getFilesList(String command) {
        return new ArrayList<>(Arrays.asList(command.split(String.valueOf(delimiter))));
    }

    private void authorization(String command) throws IOException {
        try {
            String login = command.split(String.valueOf(delimiter))[0];
            String password = command.split(String.valueOf(delimiter))[1];
            nickName = dataBaseAuthService.getNicknameByLoginAndPassword(login, password);
            List<String> nicknameAuthUser = NettyServer.nicknameAuthUsers;
            checkAlreadyAuth(nicknameAuthUser);
            nicknameAuthUser.add(nickName);
            currentPath = currentPath.resolve(nickName);
            commandSender.sendAuthOkCommand();
            commandSender.sendFilesListCommand(getFilesListFromPath(currentPath));
        } catch (AlreadyAuthorizeException e) {
            commandSender.sendErrorCommand("Already Authorize");
        } catch (UserNotFoundException e) {
            commandSender.sendErrorCommand("User Not Found");
        } catch (WrongCredentialsException e) {
            commandSender.sendErrorCommand("Wrong Credentials");
        }
    }

    private void openDirectory(String path) throws IOException {
        if (isShareDir) {
            sharePath = sharePath.resolve(path);
            if (Files.isDirectory(sharePath)) {
                if (isFirstEnterShare) startSharePath = Paths.get(ApplicationProperties.FILES_BASE_PATH, path);
                isFirstEnterShare = false;
                commandSender.sendFilesListCommand(getFilesListFromPath(sharePath));
            } else {
                Desktop.getDesktop().open(sharePath.toFile());
                sharePath = cutSharePath(sharePath, path);
            }
            return;
        }
        if (currentPath.resolve(path).toString().equals(Paths.get(ApplicationProperties.FILES_BASE_PATH, nickName, shareDir).toString())) {
            isShareDir = true;
            sharePath = Paths.get(ApplicationProperties.FILES_BASE_PATH);
            commandSender.sendFilesListCommand(shareService.loadShareFiles(nickName));
            return;
        }
        if (Files.isDirectory(currentPath.resolve(path))) {
            currentPath = currentPath.resolve(path);
//            protocolFileReceiver.setCurrentDir(currentPath.toString());
            commandSender.sendFilesListCommand(getFilesListFromPath(currentPath));
        } else {
            Desktop.getDesktop().open(currentPath.resolve(path).toFile());
        }
    }

    private Path cutSharePath(Path sharePath, String path) {
        return Paths.get(sharePath.toString().substring(0, sharePath.toString().length() - path.length()));
    }

//    private void openShareDir(ChannelHandlerContext ctx, String path) throws IOException {
//        System.out.println("path " + path);
//        Path pathToOpen = sharePath.resolve(path);
//        System.out.println("share path " + sharePath.toString());
//        System.out.println("path to open " + pathToOpen.toString());
//        if (Files.isDirectory(pathToOpen)) {
//            sharePath = sharePath.resolve(path);
//            System.out.println("Open share path " + sharePath.toString());
//            if (isFirstEnterShare) {
//                pathFile = sharePath;
//                isFirstEnterShare = false;
//                System.out.println("Open share path first enter share " + pathFile.toString());
//            }
//            CommandSender.sendFilesListCommand(ctx.channel(), getFilesListFromPath(sharePath));
//        } else {
//            try {
//                Desktop.getDesktop().open(pathToOpen.toFile());
//            } catch (IllegalArgumentException e) {
//                CommandSender.sendErrorCommand(ctx.channel(), e.getMessage());
////                shareService.deleteFile(path);
//            }
//        }
//    }

    private void goBackDir(ChannelHandlerContext ctx) throws IOException {
        if (sharePath.toString().equals
                (ApplicationProperties.FILES_BASE_PATH)) {
            commandSender.sendFilesListCommand(getFilesListFromPath(currentPath));
            sharePath = Paths.get("");
            isShareDir = false;
            return;
        }
        if (isShareDir) {
            System.out.println("share");
            if (sharePath.toString().equals(startSharePath.toString())) {
                System.out.println("first share");
                isFirstEnterShare = true;
                sharePath = Paths.get(ApplicationProperties.FILES_BASE_PATH);
                commandSender.sendFilesListCommand(shareService.loadShareFiles(nickName));
                return;
            }
            sharePath = sharePath.getParent();
            commandSender.sendFilesListCommand(getFilesListFromPath(sharePath));
            return;
        }
        if (currentPath.equals(Paths.get(basePath, nickName))) {
            System.out.println("cur");
            return;
        }
        currentPath = currentPath.getParent();
//        protocolFileReceiver.setCurrentDir(currentPath.toString());
        commandSender.sendFilesListCommand(getFilesListFromPath(currentPath));
    }

    private List<String> getFilesListFromPath(Path path) {
        try {
            return Files.list(path)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void checkAlreadyAuth(List<String> nicknameAuthUser) {
        for (String s : nicknameAuthUser) {
            if (s.equals(nickName)) {
                throw new AlreadyAuthorizeException("Already Authorization");
            }
        }
    }

    private void shareFile(ChannelHandlerContext ctx, String command) {
        try {
            List<String> filesAndNickname = getFilesList(command);
            String nickname = filesAndNickname.remove(filesAndNickname.size() - 1);
            shareService.shareFile(nickname, filesAndNickname, currentPath);
//            System.out.println(filesAndNickname);
//            System.out.println(nickname);
//            fileCopier.copyFiles(filesAndNickname, currentPath, Paths.get(basePath, nickname, "SHARING", "(from " + this.nickName + ")"));

//            Files.copy(currentPath.resolve(filename), Paths.get(basePath, nickname, "SHARING", "(from " + nickname + ")" + filename));
        } catch (ShareNotAvailableException e) {
            commandSender.sendErrorCommand("File With This Name exists");
        } catch (UserNotFoundException e) {
            commandSender.sendErrorCommand("User Not Found");
        }
    }

    private boolean checkShareDir(String pathName) {
        return (pathName.equals(Paths.get(basePath, nickName, shareDir).toString()));
    }

}
