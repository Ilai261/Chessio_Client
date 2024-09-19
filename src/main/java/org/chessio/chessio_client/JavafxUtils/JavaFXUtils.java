// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class has specifically one function that creates an alert window with a http response from the server

package org.chessio.chessio_client.JavafxUtils;

import javafx.scene.control.Alert;

import java.net.http.HttpResponse;

public class JavaFXUtils
{
    // creates an alert window for a certain http response from the server
    public static void createAlert(HttpResponse<String> response)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login failed: " + response.body());
        alert.setHeaderText(null);
        alert.setContentText(response.body());
        alert.showAndWait();
    }
}
