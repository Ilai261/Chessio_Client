package org.chessio.chessio_client.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;


public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private void handleRegister() throws IOException{
        // Retrieve the text from the username and password fields
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Example logic for handling login (replace with your actual logic)
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // Clear the text fields after login logic
        usernameField.clear();
        passwordField.clear();
        if(username.equals("admin") && password.equals("admin")){
            // Instead of showing an alert, open a new window for the chess board
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/home_screen.fxml"));
            Parent root = null;
            root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Chess Board");
            stage.setScene(new Scene(root));
            stage.show();
        }
    }


}
