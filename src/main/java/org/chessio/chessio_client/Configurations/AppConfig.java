// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class communicates with the application.properties file to retrieve server address and port

package org.chessio.chessio_client.Configurations;

import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    // load the properties
    static {
        try {
            properties.load(AppConfig.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            System.err.println("Error loading application.properties: " + e.getMessage());
        }
    }

    public static String getServerAddress() {
        return properties.getProperty("server.address");
    }

    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "44444"));
    }
}
