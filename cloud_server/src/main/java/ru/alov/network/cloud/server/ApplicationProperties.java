package ru.alov.network.cloud.server;

import ru.alov.network.cloud.common.properties_provider.PropertiesProvider;
import ru.alov.network.cloud.server.properties_provider.ServerPropertiesProvider;

import java.util.Properties;

public class ApplicationProperties {

    private static final String APPLICATION_PROPERTIES_PATH;

    public static final String SERVER_HOST;

    public static final int SERVER_PORT;

    public static final String FILES_BASE_PATH;

    public static final int BUFF_SIZE;

    public static final int MAX_OBJECT_DECODING_SIZE;

    public static final String DATABASE_URL;

    public static final String DATABASE_USERNAME;

    public static final String DATABASE_PASSWORD;


    static {
        APPLICATION_PROPERTIES_PATH = "cloud_server/src/main/resources/application.properties";

        PropertiesProvider propertiesProvider = ServerPropertiesProvider.getInstance(APPLICATION_PROPERTIES_PATH);

        Properties properties = propertiesProvider.readProperties();

        SERVER_HOST = properties.getProperty("server.host");

        SERVER_PORT = Integer.parseInt(properties.getProperty("server.port"));

        FILES_BASE_PATH = properties.getProperty("server.files_base_path");

        BUFF_SIZE = Integer.parseInt(properties.getProperty("server.buff_size"));

        MAX_OBJECT_DECODING_SIZE = Integer.parseInt(properties.getProperty("server.max_object_decoding_size"));

        DATABASE_URL = properties.getProperty("database.url");

        DATABASE_USERNAME = properties.getProperty("database.username");

        DATABASE_PASSWORD = properties.getProperty("database.password");
    }

}
