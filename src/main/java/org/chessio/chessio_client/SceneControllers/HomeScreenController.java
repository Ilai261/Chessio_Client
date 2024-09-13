package org.chessio.chessio_client.SceneControllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeScreenController {

    @FXML
    void botPressed(MouseEvent event) {
        try {
            // Open the settings screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/choose_settings.fxml"));
            //?We should simply use here an outside function that moves between scenes to have less code,
            // make this a scene instead of a pop-up
            Parent root = loader.load();

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

    // Other methods...
}
