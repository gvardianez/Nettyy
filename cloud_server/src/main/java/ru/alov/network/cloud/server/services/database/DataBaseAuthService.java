package ru.alov.network.cloud.server.services.database;

import ru.alov.network.cloud.server.exeptions.LoginIsNotAvailableException;
import ru.alov.network.cloud.server.exeptions.NicknameIsNotAvailableException;
import ru.alov.network.cloud.server.exeptions.UserNotFoundException;
import ru.alov.network.cloud.server.exeptions.WrongCredentialsException;

import java.sql.*;

class DataBaseAuthService implements AuthorizationService {

    private static Connection connection;
    private final PreparedStatement selectNicknameByLogin;
    private final PreparedStatement selectNicknameByLoginAndPassword;
    private final PreparedStatement checkLogin;
    private final PreparedStatement checkNickName;
    private final PreparedStatement createNewUser;

    private ResultSet resultSet;

    public static AuthorizationService getInstance(Connection con) {
        connection = con;
        return new DataBaseAuthService();
    }

    private DataBaseAuthService() {
        try {
            selectNicknameByLogin = connection.prepareStatement("select nickname from users where login = ?");
            selectNicknameByLoginAndPassword = connection.prepareStatement("select nickname from users where login = ? and password = ?");
            checkLogin = connection.prepareStatement("select login from users where login = ?;");
            checkNickName = connection.prepareStatement("select nickname from users where nickname = ?;");
            createNewUser = connection.prepareStatement("INSERT INTO users (nickname, login, password) VALUES (?, ?, ?);");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            selectNicknameByLogin.setString(1, login);
            resultSet = selectNicknameByLogin.executeQuery();
            if (!resultSet.next()) throw new UserNotFoundException("");
            selectNicknameByLoginAndPassword.setString(1, login);
            selectNicknameByLoginAndPassword.setString(2, password);
            resultSet = selectNicknameByLoginAndPassword.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            } else {
                resultSet.close();
                throw new WrongCredentialsException("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void createNewUser(String login, String password, String nickname) {
        try {
            checkLogin.setString(1, login);
            resultSet = checkLogin.executeQuery();
            if (resultSet.next()) throw new LoginIsNotAvailableException("");
            checkNickName.setString(1, nickname);
            resultSet = checkNickName.executeQuery();
            if (resultSet.next()) throw new NicknameIsNotAvailableException("");
            createNewUser.setString(1, nickname);
            createNewUser.setString(2, login);
            createNewUser.setString(3, password);
            createNewUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
