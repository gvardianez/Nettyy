package ru.alov.network.cloud.server.services.database;

import ru.alov.network.cloud.server.ApplicationProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseService {

    public static final Connection CONNECTION;

    static {
        try {
            CONNECTION = DriverManager.getConnection(ApplicationProperties.DATABASE_URL,
                    ApplicationProperties.DATABASE_USERNAME,
                    ApplicationProperties.DATABASE_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static AuthorizationService getAuthorizationService() {
        return DataBaseAuthService.getInstance(CONNECTION);
    }

    public static ShareService getShareService() {
        return ShareBaseService.getInstance(CONNECTION);
    }

    public static void stop() {
        if (CONNECTION != null) {
            try {
                CONNECTION.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
