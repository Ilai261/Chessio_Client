package org.chessio.chessio_client.Configurations;

import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(AppConfig.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getServerAddress() {
        return properties.getProperty("server.address");
    }

    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "44444"));
    }
}
