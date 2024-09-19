// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class creates the screen that shows the game history for a user, and is given by a request from the server.

package org.chessio.chessio_client.SceneControllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.chessio.chessio_client.Models.GameHistoryRequest;
import org.chessio.chessio_client.Models.GameSummary;
import org.chessio.chessio_client.Services.ClientHttpSender;

import java.net.http.HttpResponse;
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
            // Initializes the table columns
            player1Column.setCellValueFactory(new PropertyValueFactory<>("player1"));
            player2Column.setCellValueFactory(new PropertyValueFactory<>("player2"));
            winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winner"));

            // send plain text request with username
            GameHistoryRequest gameHistoryRequest = new GameHistoryRequest(username);

            // start the asynchronous task to send the request
            var gameHistoryTask = clientHttpSender.postAsync("/game_history", gameHistoryRequest);

            // set success handler for the task
            gameHistoryTask.setOnSucceeded(_ -> {
                try {
                    HttpResponse<String> response = gameHistoryTask.getValue();
                    // Debug: Log the full response

                    // Handle the response
                    handleResponse(response);
                } catch (Exception e) {
                    // Debug: Log any exceptions that occur while handling the response
                    System.out.println("An error occurred while performing the operation:" + " opening game history");
                }
            });

            // Set failure handler for the task
            gameHistoryTask.setOnFailed(_ -> System.out.println("An error occurred while performing the operation:" + " failes game history"));

            // Debug: Start the task in a new thread
            new Thread(gameHistoryTask).start();

        } catch (Exception e) {
            // Debug: Log any exceptions in the initialization method
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

                // update the leaderboard table with the retrieved game summaries
                Platform.runLater(() -> {
                    leaderboardTable.setItems(FXCollections.observableArrayList(games));
                });
            } else {
                System.out.println("Server returned an error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error handling server response: ");
        }
    }


    @FXML
    private void handleBackToHomeAction() {
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
            System.out.println("An error occurred while performing the operation:" + " changing to go To HomeScreen");
        }
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
