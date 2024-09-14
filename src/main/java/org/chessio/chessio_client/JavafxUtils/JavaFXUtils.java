package org.chessio.chessio_client.JavafxUtils;

import javafx.scene.control.Alert;

import java.net.http.HttpResponse;

public class JavaFXUtils
{
    public static void createAlert(HttpResponse<String> response)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login failed: " + response.body());
        alert.setHeaderText(null);
        alert.setContentText(response.body());
        alert.showAndWait();
    }
}
