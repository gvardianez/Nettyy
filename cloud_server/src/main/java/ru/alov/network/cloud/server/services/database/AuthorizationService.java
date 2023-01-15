package ru.alov.network.cloud.server.services.database;

import java.sql.SQLException;

public interface AuthorizationService {

    String getNicknameByLoginAndPassword(String login, String password);

    void createNewUser(String login, String password, String nickname);
}
