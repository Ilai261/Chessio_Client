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
        fxmlFiles.put("Login","login.fxml");
        fxmlFiles.put("Register","register.fxml");
        fxmlFiles.put("LeaderBoard","leaderboard.fxml");
        fxmlFiles.put("ChessBoard","settings.fxml");
        fxmlFiles.put("HomeScreen","home_screen.fxml");
    }

    @FXML
    private void handleLogin() throws IOException{
        // Retrieve the text from the username and password fields
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Example logic for handling login (replace with your actual logic)
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // Clear the text fields after login logic
        usernameField.clear();
        passwordField.clear();


        // Instead of showing an alert, open a new window for the chess board
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/chessBoard.fxml"));
        Parent root = null;
        root = fxmlLoader.load();

        Stage stage = new Stage();
        stage.setTitle("Chess Board");
        stage.setScene(new Scene(root));
        stage.show();
        System.out.println("From Login to chessBoard");
    }

    @FXML
    private void handleRegister() throws IOException {
        System.out.println("Register button clicked");

        // Disable the button after it is clicked
        registerButton.setDisable(true);

        // Load the register.fxml and show it in a new stage
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/register.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Register");
        stage.setScene(new Scene(root));

        // Re-enable the register button when the window is closed
        stage.setOnHidden(event -> registerButton.setDisable(false));

        // Show the stage (window)
        stage.show();
    }

    public void openNewScene(Button btn) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/leaderboard.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Leaderboard");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
