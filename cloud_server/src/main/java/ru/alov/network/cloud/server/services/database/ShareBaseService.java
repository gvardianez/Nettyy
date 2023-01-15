package ru.alov.network.cloud.server.services.database;

import ru.alov.network.cloud.server.ApplicationProperties;
import ru.alov.network.cloud.server.exeptions.ShareNotAvailableException;
import ru.alov.network.cloud.server.exeptions.UserNotFoundException;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class ShareBaseService implements ShareService {

    private static Connection connection;
    private final PreparedStatement selectIdUserByNickname;
    private final PreparedStatement createNewShare;
    private final PreparedStatement selectShareFiles;
    private final PreparedStatement deleteShareByFilepath;

    private ResultSet resultSet;

    public static ShareService getInstance(Connection con) {
        connection = con;
        return new ShareBaseService();
    }

    private ShareBaseService() {
        try {
            selectIdUserByNickname = connection.prepareStatement("select idUsers from users where nickname = ?;");
            createNewShare = connection.prepareStatement("insert into share (idUser, filename, filepath) values (?, ?, ?);");
            selectShareFiles = connection.prepareStatement("select idShare,filepath from share join users on idUser = idUsers where nickname = ?;");
            deleteShareByFilepath = connection.prepareStatement("delete from share where filepath = ?;");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void shareFile(String nickName, List<String> fileNames, Path filePath) {
        int idUser;
        String stringFilePath;
        try {
            selectIdUserByNickname.setString(1, nickName);
            resultSet = selectIdUserByNickname.executeQuery();
            if (!resultSet.next()) throw new UserNotFoundException("");
            idUser = resultSet.getInt("idUsers");
            for (String fileName : fileNames) {
                stringFilePath = filePath.resolve(fileName).toString().substring(ApplicationProperties.FILES_BASE_PATH.length() + 1);
                createNewShare.setInt(1, idUser);
                createNewShare.setString(2, fileName);
                createNewShare.setString(3, stringFilePath);
                createNewShare.executeUpdate();
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ShareNotAvailableException("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> loadShareFiles(String nickName) {
        List<String> fileNames = new ArrayList<>();
        try {
            selectShareFiles.setString(1, nickName);
            resultSet = selectShareFiles.executeQuery();
            while (resultSet.next()) {
                fileNames.add((resultSet.getString("filepath")));
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            deleteShareByFilepath.setString(1, filePath);
            deleteShareByFilepath.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
