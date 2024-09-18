package org.chessio.chessio_client.SceneControllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.chessio.chessio_client.Models.GameSummary;

public class GameHistoryController {

    @FXML
    private TableView<GameSummary> leaderboardTable;

    @FXML
    private TableColumn<GameSummary, String> player1Column;

    @FXML
    private TableColumn<GameSummary, String> player2Column;

    @FXML
    private TableColumn<GameSummary, String> winnerColumn;

    @FXML
    public void initialize() {
        // Initialize the table columns
        player1Column.setCellValueFactory(new PropertyValueFactory<>("player1"));
        player2Column.setCellValueFactory(new PropertyValueFactory<>("player2"));
        winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winner"));

        // Add some sample data (optional, replace with actual data)
        leaderboardTable.getItems().add(new GameSummary("Alice", "Bob", "Alice"));
        leaderboardTable.getItems().add(new GameSummary("Charlie", "David", "David"));
    }

    @FXML
    private void handleBackToHomeAction(MouseEvent event) {
        try {
            // Load the home screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/homeScreen.fxml"));
            Parent homeScreen = loader.load();
            Stage stage = (Stage) leaderboardTable.getScene().getWindow();
            stage.setScene(new Scene(homeScreen));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
