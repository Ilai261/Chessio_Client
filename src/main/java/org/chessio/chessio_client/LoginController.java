package org.chessio.chessio_client;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private void handleLogin() {
        // Retrieve the text from the username and password fields
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Example logic for handling login (replace with your actual logic)
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // Clear the text fields after login logic
        usernameField.clear();
        passwordField.clear();
    }

    @FXML
    private void handleRegister() {
        // Handle the registration logic here
        System.out.println("Register button clicked");
    }
}
