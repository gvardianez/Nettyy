package ru.alov.network.cloud.server.properties_provider;

import ru.alov.network.cloud.common.properties_provider.PropertiesProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ServerPropertiesProvider implements PropertiesProvider {

    private final String propertiesPath;

    public static PropertiesProvider getInstance(String propertiesPath){
        return new ServerPropertiesProvider(propertiesPath);
    }

    private ServerPropertiesProvider(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    @Override
    public Properties readProperties() {
        try (FileInputStream fis = new FileInputStream(propertiesPath)) {
            Properties properties = new Properties();
            properties.load(fis);
           return properties;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


}
