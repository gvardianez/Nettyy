package ru.alov.network.cloud.server.services.bd_migration;

import org.flywaydb.core.Flyway;

public class FlywayBaseMigration implements BaseMigrationProvider {

    private final String CONNECTION_URL;

    private final String NAME;

    private final String PASS;

    public FlywayBaseMigration(String connection_url, String name, String pass) {
        CONNECTION_URL = connection_url;
        NAME = name;
        PASS = pass;
    }

    @Override
    public void migrate() {
        Flyway flyway = Flyway.configure().dataSource(CONNECTION_URL, NAME, PASS).load();
        flyway.migrate();
    }

}
