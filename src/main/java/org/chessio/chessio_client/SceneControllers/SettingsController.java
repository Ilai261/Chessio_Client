package org.chessio.chessio_client.SceneControllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    @FXML
    private Slider levelSlider; // Slider for enemy level

    @FXML
    private RadioButton whiteRadioButton; // RadioButton for white pieces

    @FXML
    private RadioButton blackRadioButton; // RadioButton for black pieces

    private Stage homeStage; // Reference to the HomeScreenController stage

    private String selectedColor; // To hold the selected color
    private int enemyLevel; // To hold the selected enemy level

    // Set the home stage from HomeScreenController
    public void setHomeStage(Stage homeStage) {
        this.homeStage = homeStage;
    }

    @FXML
    public void initialize() {
        // Create and set the ToggleGroup programmatically
        ToggleGroup colorToggleGroup = new ToggleGroup();
        whiteRadioButton.setToggleGroup(colorToggleGroup);
        blackRadioButton.setToggleGroup(colorToggleGroup);

        // Set default values for color and level
        selectedColor = "white"; // Default color is white
        enemyLevel = (int) levelSlider.getValue(); // Get the initial slider value

        // Listener for color selection
        colorToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == whiteRadioButton) {
                selectedColor = "white";
            } else {
                selectedColor = "black";
            }
        });

        // Listener for the slider value
        levelSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            enemyLevel = newValue.intValue();
        });
    }

    // Method to handle the "Start Game" button click
    @FXML
    private void startGame() {
        // Print the selected color and level for debugging
        System.out.println("Selected Color: " + selectedColor);
        System.out.println("Selected Enemy Level: " + enemyLevel);

        // Close the HomeScreen stage
        if (homeStage != null) {
            homeStage.close();
        }

        // Close the current settings window
        Stage settingsStage = (Stage) levelSlider.getScene().getWindow();
        settingsStage.close();

        // Load the ChessBoardController with the selected color and enemy level
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/chessBoard.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the selected settings
            BotBoardController chessBoardController = loader.getController();
            chessBoardController.initializeGame(selectedColor, enemyLevel); // Method to initialize the game with color and level

            // Open the chessboard screen
            Stage gameStage = new Stage();
            gameStage.setTitle("Chess GraphicsBoard");
            gameStage.setScene(new Scene(root));
            gameStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
