package org.chessio.chessio_client.SceneControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeScreenController
{

    public AnchorPane anchorPane;
    private String username;

    @FXML
    void botPressed(MouseEvent event) {

        try {
            // Open the settings screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/chooseSettings.fxml"));
            Parent root = loader.load();

            // Pass the HomeScreen stage to the SettingsController
            SettingsController settingsController = loader.getController();
            Stage homeStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            settingsController.setHomeStage(homeStage);
            // Get the controller and pass the username
            settingsController.setUsername(username); // Replace "PlayerUsername" with the actual username

            Stage stage = new Stage();
            stage.setTitle("Choose Settings");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onlinePressed(MouseEvent event) {
        try {
            // Load the waiting room scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/waitingRoom.fxml"));
            Parent waitingRoom = loader.load();

            // Get the controller and pass the username
            WaitingRoomController waitingRoomController = loader.getController();
            waitingRoomController.initialize(username);

            // Show the waiting room scene
            Stage stage = (Stage) anchorPane.getScene().getWindow(); // Assuming the gridPane is already part of the scene
            stage.setScene(new Scene(waitingRoom));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void quitPressed(MouseEvent event) {
        // Handle quit functionality
        Platform.exit();
    }

    void setUsername(String username) {
        this.username = username;
    }

    @FXML
    void openGameHistory(MouseEvent event) {
        try {
            // Load the GameHistory scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/GameHistory.fxml"));
            Parent gameHistory = loader.load();

            GameHistoryController gameHistoryController = loader.getController();
            gameHistoryController.initialize(username);
            gameHistoryController.setUsername(username);

            // Close the current HomeScreen and open GameHistory in the same window
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(new Scene(gameHistory));
            stage.show();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
