package org.chessio.chessio_client.SceneControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.chessio.chessio_client.Models.RegistrationRequest;
import org.chessio.chessio_client.Services.ClientHttpSender;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.chessio.chessio_client.JavafxUtils.JavaFXUtils.createAlert;
// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class is handling the registry with communication to the server, and allows to go back to the login screen.
public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button registerButton;

    private ClientHttpSender clientHttpSender;

    public void initialize()
    {
        clientHttpSender = new ClientHttpSender();
    }

    @FXML
    private void handleRegister() {
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

        RegistrationRequest registrationRequest = new RegistrationRequest(username, password);//handled in the server

        var registerTask = clientHttpSender.postAsync("/register", registrationRequest);

        registerTask.setOnSucceeded(_ -> {
            HttpResponse<String> response = registerTask.getValue();
            handleResponse(response, username);
        });

        registerTask.setOnFailed(_ -> {
            Throwable e = registerTask.getException();
            handleError(e);
        });

        new Thread(registerTask).start();

        clearFields();//after registery
    }

    private void handleResponse(HttpResponse<String> response, String username)
    {
        String REGISTER_SUCCESSFUL = "registered_successfully";
        if (response.statusCode() == 200 && response.body().equals(REGISTER_SUCCESSFUL)) {
            System.out.println("Registration successful");
            Platform.runLater(() -> {
                try
                {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/homeScreen.fxml"));
                    Parent root = loader.load();

                    // gets the controller and pass the selected settings
                    HomeScreenController homeScreenController = loader.getController();
                    homeScreenController.setUsername(username);

                    // Gets the current stage (instead of opening a new window)
                    Stage stage = (Stage) registerButton.getScene().getWindow(); // Assuming 'gridPane' is a node in the current scene

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
            createAlert(response);
            System.out.println("Registration failed: " + response.body());
        }
    }

    private void handleError(Throwable e) {
        System.out.println("Error during registration: " + e.getMessage());
        System.out.println("Connection error: " + e.getMessage());
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }

    public void openNewScene(String fxmlFilePath) throws IOException {
        // loads the new FXML file
        clientHttpSender.shutdown();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
        Parent root = loader.load();

        // gets the current stage using any node (e.g., loginButton or registerButton)
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleLoginLink() throws IOException{
        System.out.println("Register button clicked");
        openNewScene("/org/chessio/chessio_client/login.fxml");//goes back to login screen
    }
}
