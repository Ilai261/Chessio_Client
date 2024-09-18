package org.chessio.chessio_client.SceneControllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.chessio.chessio_client.JavafxUtils.JavaFXUtils;
import org.chessio.chessio_client.Models.GameHistoryRequest;
import org.chessio.chessio_client.Models.GameSummary;
import org.chessio.chessio_client.Services.ClientHttpSender;

import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;

public class GameHistoryController {
    @FXML
    private VBox vbox;

    @FXML
    private TableView<GameSummary> leaderboardTable;

    @FXML
    private TableColumn<GameSummary, String> player1Column;

    @FXML
    private TableColumn<GameSummary, String> player2Column;

    @FXML
    private TableColumn<GameSummary, String> winnerColumn;

    private String username;

    private ClientHttpSender clientHttpSender = new ClientHttpSender();

    @FXML
    public void initialize(String username) {
        try {
            // Initialize the table columns
            player1Column.setCellValueFactory(new PropertyValueFactory<>("player1"));
            player2Column.setCellValueFactory(new PropertyValueFactory<>("player2"));
            winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winner"));

            // Send plain text request (username)
            GameHistoryRequest gameHistoryRequest = new GameHistoryRequest(username);

            // Start the asynchronous task to send the request
            var gameHistoryTask = clientHttpSender.postAsync("/game_history", gameHistoryRequest);

            // Set success handler for the task
            gameHistoryTask.setOnSucceeded(event -> {
                try {
                    HttpResponse<String> response = gameHistoryTask.getValue();
                    // Debug: Log the full response

                    // Handle the response
                    handleResponse(response);
                } catch (Exception e) {
                    // Debug: Log any exceptions that occur while handling the response
                    e.printStackTrace();
                }
            });

            // Set failure handler for the task
            gameHistoryTask.setOnFailed(event -> {
                Throwable e = gameHistoryTask.getException();
                // Debug: Log the failure details
                e.printStackTrace();
            });

            // Debug: Start the task in a new thread
            new Thread(gameHistoryTask).start();

        } catch (Exception e) {
            // Debug: Log any exceptions in the initialization method
            e.printStackTrace();
        }
    }

    private void handleResponse(HttpResponse<String> response) {
        try {
            // Debug: Check if the server response is successful
            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // Debug: Log the response body
                System.out.println("Response from server: " + responseBody);

                // Check if the response body is empty before parsing
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    System.out.println("No content in the response");
                    return;
                }

                // Parse the JSON response into a list of GameSummary objects
                ObjectMapper objectMapper = new ObjectMapper();
                List<GameSummary> games = objectMapper.readValue(responseBody, new TypeReference<List<GameSummary>>() {});

                // Debug: Log the parsed games
                System.out.println("Parsed game summaries: " + games);

                // Update the leaderboard table with the retrieved game summaries
                Platform.runLater(() -> {
                    leaderboardTable.setItems(FXCollections.observableArrayList(games));
                });
            } else {
                // Debug: Log the server error status
                System.out.println("Server returned an error: " + response.statusCode());
            }
        } catch (Exception e) {
            // Debug: Log any exceptions during the response handling
            System.err.println("Error handling server response: ");
            e.printStackTrace();
        }
    }


    @FXML
    private void handleBackToHomeAction(ActionEvent event) {
        try {
            // Load the home screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/homeScreen.fxml"));
            Parent homeScreen = loader.load();

            HomeScreenController homeScreenController = loader.getController();
            homeScreenController.setUsername(username);

            Stage stage = (Stage) vbox.getScene().getWindow();
            stage.setScene(new Scene(homeScreen));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
