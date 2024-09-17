package org.chessio.chessio_client.SceneControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import org.chessio.chessio_client.Configurations.AppConfig;

import jakarta.websocket.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WaitingRoomController {

    private Session session;
    private String username;
    private boolean gameStarted = false;  // Flag to disable message handling

    @FXML
    private Label waitingLabel;

    public void initialize(String username) {
        this.username = username;
        connectToServer();
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to the server.");
    }

    @OnMessage
    public void onMessage(String message) {
        if (!gameStarted) {
            Platform.runLater(() -> handleServerMessage(message));  // Handle only if game hasn't started
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed: " + closeReason.getReasonPhrase());
    }

    private void handleServerMessage(String message)
    {
        String[] messageParts = message.split("\\|");

        if(messageParts.length > 0 && messageParts[0].equals("waiting_for_opponent"))
        {
            System.out.println("Waiting for opponent");
        }
        else if (messageParts.length > 1 && messageParts[1].equals("game_start"))
        {
            // Set the flag to stop processing further messages in this controller
            gameStarted = true;
            // Load the game scene after disabling message handling in this controller
            loadGameScene(message);
            System.out.println("game started");
        }
        else
        {
            System.out.println("Invalid message: " + message);
        }
    }

    private void connectToServer() {
        try
        {
            String baseurl = "ws://" + AppConfig.getServerAddress() + ":" + AppConfig.getServerPort();
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = new URI(baseurl + "/game");
            container.connectToServer(this, uri);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGameScene(String message) {
        // Load the chess game scene when opponent is found
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/onlineChessBoard.fxml"));
            Parent root = loader.load();

            // Pass the game start details to the chess board controller
            OnlineBoardController chessBoardController = loader.getController();
            chessBoardController.setUsername(username);
            chessBoardController.setSession(session);
            chessBoardController.initializeGame(message);

            // Switch the scene to the chess game
            Stage stage = (Stage) waitingLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
