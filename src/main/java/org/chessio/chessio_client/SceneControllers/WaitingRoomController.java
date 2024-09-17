package org.chessio.chessio_client.SceneControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import jakarta.websocket.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WaitingRoomController {

    private Session session;
    private String username;

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
        Platform.runLater(() -> handleServerMessage(message));
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed: " + closeReason.getReasonPhrase());
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("game_start")) {
            // Game is starting, load the chessboard scene and pass the message details
            loadGameScene(message);
        }
    }

    private void connectToServer() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = new URI("ws://localhost:8080/game");
            container.connectToServer(this, uri);
        } catch (Exception e) {
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
