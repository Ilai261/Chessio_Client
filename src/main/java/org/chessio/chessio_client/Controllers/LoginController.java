package org.chessio.chessio_client.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    public HashMap<String, String> fxmlFiles;

    @FXML
    public void initialize() {
        // Set initial values or perform any other necessary setup
        fxmlFiles = new HashMap<>();
        fxmlFiles.put("Login", "login.fxml");
        fxmlFiles.put("Register", "register.fxml");
        fxmlFiles.put("LeaderBoard", "leaderboard.fxml");
        fxmlFiles.put("ChessBoard", "settings.fxml");
        fxmlFiles.put("HomeScreen", "home_screen.fxml");
    }

    @FXML
    private void handleLogin() throws IOException {
        // Retrieve the text from the username and password fields
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Example logic for handling login (replace with your actual logic)
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // Clear the text fields after login logic
        usernameField.clear();
        passwordField.clear();

        // here we need to send a request to the server regarding the login, if successful go to homeScreen
        //?We should simply use here an outside function that moves between scenes to have less code
        openNewScene("/org/chessio/chessio_client/home_screen.fxml");
        System.out.println("From Login to homeScreen");
    }

    @FXML
    private void handleRegister() throws IOException {
        System.out.println("Register button clicked");

        // Change the scene to register.fxml instead of opening a new window
        openNewScene("/org/chessio/chessio_client/register.fxml");
    }

    private void openNewScene(String fxmlFilePath) throws IOException {
        // Load the new FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
        Parent root = loader.load();

        // Get the current stage using any node (e.g., loginButton or registerButton)
        Stage stage = (Stage) loginButton.getScene().getWindow();

        // Set the new scene on the current stage
        stage.setScene(new Scene(root));
    }

}
