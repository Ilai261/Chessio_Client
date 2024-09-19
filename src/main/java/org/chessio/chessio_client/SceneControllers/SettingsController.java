// Written by Ilai Azaria and Eitan Feldsherovich, 2024
//settings allows the user to choose difficulty level of the bot and the color that he want to play with.

package org.chessio.chessio_client.SceneControllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    @FXML
    private RadioButton level1, level2, level3, level4, level5;
    @FXML
    private RadioButton level6, level7, level8, level9, level10;
    @FXML
    private RadioButton level11, level12, level13, level14, level15;
    @FXML
    private RadioButton level16, level17, level18, level19, level20;

    @FXML
    private RadioButton whiteRadioButton; // radioButton for white pieces

    @FXML
    private RadioButton blackRadioButton; // radioButton for black pieces

    private Stage homeStage; // Reference to the HomeScreenController stage

    private String selectedColor; // to hold the selected color

    private int enemyLevel; //holds the selected enemy level

    private String username;

    // Sets the home stage from HomeScreenController
    public void setHomeStage(Stage homeStage) {
        this.homeStage = homeStage;
    }

    @FXML
    public void initialize() {
        // Array of level RadioButtons
        RadioButton[] levelButtons = {
                level1, level2, level3, level4, level5,
                level6, level7, level8, level9, level10,
                level11, level12, level13, level14, level15,
                level16, level17, level18, level19, level20
        };

        // creates and set the ToggleGroup programmatically for levels
        ToggleGroup levelToggleGroup = new ToggleGroup();
        for (RadioButton levelButton : levelButtons) {
            levelButton.setToggleGroup(levelToggleGroup);
        }

        // creates and set the ToggleGroup for color
        ToggleGroup colorToggleGroup = new ToggleGroup();
        whiteRadioButton.setToggleGroup(colorToggleGroup);
        blackRadioButton.setToggleGroup(colorToggleGroup);

        // sets default values for color and level
        selectedColor = "white"; // Default color is white
        enemyLevel = 1; // Default level is 1 (from level1 button)

        // listens for color selection by the user
        colorToggleGroup.selectedToggleProperty().addListener((_, _, newToggle) -> {
            if (newToggle == whiteRadioButton) {
                selectedColor = "white";
            } else {
                selectedColor = "black";
            }
        });

        // Listener for the level selection
        levelToggleGroup.selectedToggleProperty().addListener((_, _, newToggle) -> {
            RadioButton selectedRadioButton = (RadioButton) newToggle;
            enemyLevel = Integer.parseInt(selectedRadioButton.getText()); // Get level from the RadioButton text
        });
    }


    // Method to handle the Start Game button
    @FXML
    private void startGame() {
        // Prints the selected color and level for debugging
        System.out.println("Selected Color: " + selectedColor);
        System.out.println("Selected Enemy Level: " + enemyLevel);

        // Closes the HomeScreen stage
        if (homeStage != null) {
            homeStage.close();
        }

        // Closes the current settings window
        Stage settingsStage = (Stage) level1.getScene().getWindow();
        settingsStage.close();

        // Loads the ChessBoardController with the selected color and enemy level
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/botChessBoard.fxml"));
            Parent root = loader.load();

            // gets the controller and pass the selected settings by the user
            BotBoardController chessBoardController = loader.getController();
            chessBoardController.setEnemyLevel(enemyLevel);
            chessBoardController.setUsername(username);
            chessBoardController.setUsernameLabel(username);
            chessBoardController.initializeGame(selectedColor); // Method to initialize the game with color and level

            // opens the chessboard screen
            Stage gameStage = new Stage();
            gameStage.setTitle("ChessBoard");
            gameStage.setScene(new Scene(root));
            gameStage.show();
        } catch (IOException e) {
            System.out.println("can't open the bot chess board.");
        }
    }

    public void setUsername(String playerUsername) {
        this.username = playerUsername;
    }
}
