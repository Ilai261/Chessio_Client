// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class is handling the possible actions from the home screen fxml, which are playing against a bot,
// searching online, or viewing history of past games.

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
            // opens the settings screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/chooseSettings.fxml"));
            Parent root = loader.load();

            // Passes the HomeScreen stage to the settingscontroller
            SettingsController settingsController = loader.getController();
            Stage homeStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            settingsController.setHomeStage(homeStage);
            // gets the controller and pass the username
            settingsController.setUsername(username); // Replace "PlayerUsername" with the actual username

            Stage stage = new Stage();
            stage.setTitle("Choose Settings");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("An error occurred while performing the operation:" + " pressing bot button");
        }
    }

    @FXML
    void onlinePressed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/waitingRoom.fxml"));
            Parent waitingRoom = loader.load();

            // gets the controller and pass the username
            WaitingRoomController waitingRoomController = loader.getController();
            waitingRoomController.initialize(username);

            // Shows the waiting room scene
            Stage stage = (Stage) anchorPane.getScene().getWindow(); // Assuming the gridPane is already part of the scene
            stage.setScene(new Scene(waitingRoom));
            stage.show();
        } catch (IOException e) {
            System.out.println("An error occurred while performing the operation:" + " pressing Online button");
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
    void openGameHistory() {
        try {
            // Loads the GameHistory scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/GameHistory.fxml"));
            Parent gameHistory = loader.load();

            GameHistoryController gameHistoryController = loader.getController();
            gameHistoryController.initialize(username);
            gameHistoryController.setUsername(username);

            // closes the current HomeScreen and open GameHistory in the same window
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(new Scene(gameHistory));
            stage.show();
        } catch (IOException e)
        {
            System.out.println("An error occurred while performing the operation:" + " pressing Online button");
        }
    }
}
