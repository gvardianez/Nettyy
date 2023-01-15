package ru.alov.network.cloud.client;

import ru.alov.network.cloud.client.properties_provider.ClientPropertiesProvider;
import ru.alov.network.cloud.common.properties_provider.PropertiesProvider;

import java.util.Properties;

public class ApplicationProperties {

    private static final String APPLICATION_PROPERTIES_PATH;

    public static final String SERVER_HOST;

    public static final int SERVER_PORT;

    public static final int BUFF_SIZE;

    static {
        APPLICATION_PROPERTIES_PATH = "cloud_client/src/main/resources/application.properties";
        PropertiesProvider propertiesProvider = ClientPropertiesProvider.getInstance(APPLICATION_PROPERTIES_PATH);
        Properties properties = propertiesProvider.readProperties();
        SERVER_HOST = properties.getProperty("server.host");
        SERVER_PORT = Integer.parseInt(properties.getProperty("server.port"));
        BUFF_SIZE = Integer.parseInt(properties.getProperty("client.buff_size"));
    }

}
