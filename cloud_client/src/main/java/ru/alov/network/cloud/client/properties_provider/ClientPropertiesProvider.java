package ru.alov.network.cloud.client.properties_provider;

import ru.alov.network.cloud.common.properties_provider.PropertiesProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ClientPropertiesProvider implements PropertiesProvider {

    private final String propertiesPath;

    private ClientPropertiesProvider(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public static PropertiesProvider getInstance(String propertiesPath){
        return new ClientPropertiesProvider(propertiesPath);
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
