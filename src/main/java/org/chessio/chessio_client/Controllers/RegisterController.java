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
    private void handleRegister() throws IOException {
        // Retrieve the text from the username and password fields
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Example logic for handling login (replace with your actual logic)
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // Clear the text fields after login logic
        usernameField.clear();
        passwordField.clear();

        if (username.equals("admin") && password.equals("admin")) {
            // Change the scene to home_screen.fxml instead of opening a new window
            //?We should simply use here an outside function that moves between scenes to have less code
            openNewScene("/org/chessio/chessio_client/home_screen.fxml");
        }
    }

    public void openNewScene(String fxmlFilePath) throws IOException {
        // Load the new FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
        Parent root = loader.load();

        // Get the current stage using any node (e.g., loginButton or registerButton)
        Stage stage = (Stage) registerButton.getScene().getWindow();

        // Set the new scene on the current stage
        stage.setScene(new Scene(root));
    }
}
