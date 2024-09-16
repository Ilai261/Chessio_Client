package org.chessio.chessio_client.SceneControllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeScreenController
{

    private String username;

    @FXML
    void botPressed(MouseEvent event) {

        try {
            // Open the settings screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/choose_settings.fxml"));
            Parent root = loader.load();

            // Pass the HomeScreen stage to the SettingsController
            SettingsController settingsController = loader.getController();
            Stage homeStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            settingsController.setHomeStage(homeStage);
            // Get the controller and pass the username
            settingsController.setUsername(username); // Replace "PlayerUsername" with the actual username

            // Open the settings screen (you can replace this with a scene change)
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
        // Implement logic to handle online game button press here
    }

    @FXML
    void quitPressed(MouseEvent event) {
        // Handle quit functionality
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    void setUsername(String username) {
        this.username = username;
    }
}
