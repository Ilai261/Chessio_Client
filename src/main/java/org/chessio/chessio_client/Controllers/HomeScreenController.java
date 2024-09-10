package org.chessio.chessio_client.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeScreenController {

    @FXML
    private Button botGameButton;

    @FXML
    private VBox onlineGameBtn;

    @FXML
    private VBox quitButton;

    @FXML
    void botPressed(MouseEvent event) {
        try {
            // Load chessboard screen when bot button is pressed
            openNewScene("/org/chessio/chessio_client/chessBoard.fxml");
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
        Stage stage = (Stage) quitButton.getScene().getWindow();
        stage.close();
    }

    // Utility function to open new scenes
    public void openNewScene(String fxmlFilePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilePath));
        Parent root = loader.load();

        // Get current stage (window) from any node in the scene, e.g., botGameButton
        Stage stage = (Stage) botGameButton.getScene().getWindow();

        // Set the new scene on the current stage
        stage.setScene(new Scene(root));
    }
}
