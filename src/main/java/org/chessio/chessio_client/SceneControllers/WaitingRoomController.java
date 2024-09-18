package org.chessio.chessio_client.SceneControllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.chessio.chessio_client.Configurations.AppConfig;
import org.chessio.chessio_client.MessageHandlers.ChessioMessageHandler;

import jakarta.websocket.*;
import javafx.stage.Stage;
import org.chessio.chessio_client.MessageHandlers.OnlineBoardChessioMessageHandler;
import org.chessio.chessio_client.MessageHandlers.WaitingRoomChessioMessageHandler;

import java.io.IOException;
import java.net.URI;
import javafx.scene.image.ImageView;

import java.time.Instant;
import java.util.Objects;
@ClientEndpoint
public class WaitingRoomController {

    private Session session;
    private String username;
    private ChessioMessageHandler chessioMessageHandler;  // Current message handler

    @FXML
    private Label waitingLabel;

    private Timeline timer;

    // Start time to track the elapsed time
    private long startTimeMillis;

    // ImageView for the waiting icon
    @FXML
    private ImageView waitingIcon;

    // Label for the elapsed time display
    @FXML
    private Label timerLabel;

    // RotateTransition for the rotating animation
    private RotateTransition rotateTransition;

    public void initialize(String username) {
        this.username = username;
        connectToServer();
        startWaitingAnimation();
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to the server.");
        this.chessioMessageHandler = new WaitingRoomChessioMessageHandler(this);

        // Send the username to the server when a connection is made
        String usernameMessage = "username|" + username;
        try {
            session.getBasicRemote().sendText(usernameMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        if (chessioMessageHandler != null)
        {
            Platform.runLater(() -> chessioMessageHandler.handleMessage(message)); // Delegate to the correct handler
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed: " + closeReason.getReasonPhrase());
    }

    public void handleServerMessage(String message)
    {
        String[] messageParts = message.split("\\|");

        if(messageParts.length > 0 && messageParts[0].equals("waiting_for_opponent"))
        {
            System.out.println("Waiting for opponent");
        }
        else if (messageParts.length > 1 && messageParts[1].equals("game_start"))
        {
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
        stopWaitingAnimation();//added
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/onlineChessBoard.fxml"));
            Parent root = loader.load();

            // Pass the game start details to the chess board controller
            OnlineBoardController chessBoardController = loader.getController();
            chessBoardController.setUsername(username);
            chessBoardController.setUsernameLabel(username);

            // set the game controller as the function endpoint for new messages and initialize the game
            this.setChessioMessageHandler(new OnlineBoardChessioMessageHandler(chessBoardController));
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

    private void setChessioMessageHandler(ChessioMessageHandler chessioMessageHandler) {
        this.chessioMessageHandler = chessioMessageHandler;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    // Method to start rotating the waiting icon
    private void startWaitingAnimation() {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/org/chessio/chessio_client/Icons/wait.png")).toExternalForm());
        waitingIcon.setImage(image);
        // Initialize the start time
        startTimeMillis = System.currentTimeMillis();
        // Set up the rotation animation (180 degrees every 100 ms)
        rotateTransition = new RotateTransition(Duration.millis(800), waitingIcon);
        rotateTransition.setByAngle(180);
        rotateTransition.setCycleCount(Animation.INDEFINITE);  // Keep rotating
        rotateTransition.play();
        // Set up the timeline to update the elapsed time every second
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
            timerLabel.setText("Time elapsed: " + elapsedSeconds + " seconds");
        }));
        timer.setCycleCount(Timeline.INDEFINITE);  // Keep counting time
        timer.play();
    }
    private void stopWaitingAnimation() {
        if (rotateTransition != null) {
            rotateTransition.stop();
        }
        if (timer != null) {
            timer.stop();
        }
    }
}
