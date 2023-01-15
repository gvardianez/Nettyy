package ru.alov.network.cloud.server;

import ru.alov.network.cloud.server.netty_server.NettyServer;
import ru.alov.network.cloud.server.services.bd_migration.BaseMigrationProvider;
import ru.alov.network.cloud.server.services.bd_migration.FlywayBaseMigration;

public class ServerApp {

    public static void main(String[] args) {
        BaseMigrationProvider baseMigrationProvider = new FlywayBaseMigration(
                ApplicationProperties.DATABASE_URL,
                ApplicationProperties.DATABASE_USERNAME,
                ApplicationProperties.DATABASE_PASSWORD);
        baseMigrationProvider.migrate();

        NettyServer.getServer().start();
    }
}
