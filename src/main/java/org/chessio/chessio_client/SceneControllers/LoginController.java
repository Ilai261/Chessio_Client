// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class is handling the logging in and uses the server to authenticate, and allows unregistered users
// to go to registery screen.

package org.chessio.chessio_client.SceneControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.chessio.chessio_client.Models.LoginRequest;
import org.chessio.chessio_client.Services.ClientHttpSender;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import org.chessio.chessio_client.JavafxUtils.JavaFXUtils;

public class LoginController
{

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    private ClientHttpSender clientHttpSender;

    public HashMap<String, String> fxmlFiles;

    @FXML
    public void initialize() {
        // set initials values or perform any other necessary setup
        fxmlFiles = new HashMap<>();
        fxmlFiles.put("Login", "login.fxml");
        fxmlFiles.put("Register", "register.fxml");
        fxmlFiles.put("LeaderBoard", "GameHistory.fxml");
        fxmlFiles.put("ChessBoard", "settings.fxml");
        fxmlFiles.put("HomeScreen", "homeScreen.fxml");

        clientHttpSender = new ClientHttpSender();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // makes a check to see if one of the fields is empty
        if(username.isEmpty() || password.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("no username or password entered!");
            alert.setHeaderText(null);
            alert.setContentText("enter both fields to register...");
            alert.showAndWait();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username, password);

        var loginTask = clientHttpSender.postAsync("/login", loginRequest);

        loginTask.setOnSucceeded(_ -> {
            HttpResponse<String> response = loginTask.getValue();
            handleResponse(response, username);
        });

        loginTask.setOnFailed(_ -> {
            Throwable e = loginTask.getException();
            handleError(e);
        });

        new Thread(loginTask).start();

        clearFields();
    }

    private void handleResponse(HttpResponse<String> response, String username)
    {
        String LOGIN_SUCCESSFUL = "login_successful";
        if (response.statusCode() == 200 && response.body().equals(LOGIN_SUCCESSFUL)) {
            System.out.println("Login successful");
            Platform.runLater(() -> {
                try
                {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/homeScreen.fxml"));
                    Parent root = loader.load();

                    // Gets the controller and pass the selected settings
                    HomeScreenController homeScreenController = loader.getController();
                    homeScreenController.setUsername(username);

                    //moves to the current stage (instead of opening a new window)
                    Stage stage = (Stage) loginButton.getScene().getWindow(); // Assuming 'gridPane' is a node in the current scene

                    // Set the new scene in the current stage
                    stage.setScene(new Scene(root));
                    stage.show();
                }
                catch (IOException e) {
                    System.out.println("Error opening home screen: " + e.getMessage());
                }
            });
        }
        else
        {
            JavaFXUtils.createAlert(response);
            System.out.println("Login failed: " + response.body());
        }
    }

    private void handleError(Throwable e) {
        System.out.println("Error during login: " + e.getMessage());
        System.out.println("Connection error: " + e.getMessage());
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }

    @FXML
    private void handleRegister() throws IOException {
        System.out.println("Register button clicked");

        // Change the scene to register.fxml instead of opening a new window
        openNewScene();
    }

    private void openNewScene() throws IOException {
        // loads the new FXML file
        clientHttpSender.shutdown();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/register.fxml"));
        Parent root = loader.load();

        // Gets the current stage using any node (e.g., loginButton or registerButton)
        Stage stage = (Stage) loginButton.getScene().getWindow();

        // sets the new scene on the current stage
        stage.setScene(new Scene(root));
    }

}
