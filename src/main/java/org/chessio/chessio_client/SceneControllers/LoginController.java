package org.chessio.chessio_client.SceneControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.chessio.chessio_client.Models.LoginRequest;
import org.chessio.chessio_client.Services.ClientHttpSender;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class LoginController
{

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private ClientHttpSender clientHttpSender;

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

        clientHttpSender = new ClientHttpSender();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        LoginRequest loginRequest = new LoginRequest(username, password);

        var loginTask = clientHttpSender.postAsync("/login", loginRequest);

        loginTask.setOnSucceeded(event -> {
            HttpResponse<String> response = loginTask.getValue();
            handleResponse(response);
        });

        loginTask.setOnFailed(event -> {
            Throwable e = loginTask.getException();
            handleError(e);
        });

        new Thread(loginTask).start();

        clearFields();
    }

    private void handleResponse(HttpResponse<String> response)
    {
        String LOGIN_SUCCESSFUL = "login_successful";
        if (response.statusCode() == 200 && response.body().equals(LOGIN_SUCCESSFUL)) {
            System.out.println("Login successful");
            Platform.runLater(() -> {
                try
                {
                    openNewScene("/org/chessio/chessio_client/home_screen.fxml");
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error opening home screen: " + e.getMessage());
                }
            });
        }
        else
        {
            System.out.println("Login failed: " + response.body());
        }
    }

    private void handleError(Throwable e) {
        System.out.println("Error during login: " + e.getMessage());
        System.out.println("Connection error: " + e.getMessage());
        e.printStackTrace();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }

    @FXML
    private void handleRegister() throws IOException {
        System.out.println("Register button clicked");

        // Change the scene to register.fxml instead of opening a new window
        openNewScene("/org/chessio/chessio_client/register.fxml");
    }

    private void openNewScene(String fxmlFilePath) throws IOException {
        // Load the new FXML file
        clientHttpSender.shutdown();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
        Parent root = loader.load();

        // Get the current stage using any node (e.g., loginButton or registerButton)
        Stage stage = (Stage) loginButton.getScene().getWindow();

        // Set the new scene on the current stage
        stage.setScene(new Scene(root));
    }

}
