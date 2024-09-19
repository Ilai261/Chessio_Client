package org.chessio.chessio_client.SceneControllers;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import jakarta.websocket.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.chessio.chessio_client.Models.GraphicsBoard;

import java.io.IOException;
import java.util.*;

@ClientEndpoint
public class OnlineBoardController extends BaseBoardController {

    private Session session;
    private String currentEnemyMoveMessage;
    private UUID gameId;
    private boolean canOfferDraw = true;  // Flag to track draw offer cooldown
    @FXML
    private Label timerLabelOnline;  // Label for displaying the timer

    private Timeline timer;  // To update the timer every second
    private long startTimeMillis;  // To track the start time

    @Override
    public void initializeGame(String gameStartMessage)
    {
        String[] parts = gameStartMessage.split("\\|");

        gameId = UUID.fromString(parts[0]);
        isPlayerBlack = parts[2].equals("black");
        enemyLabel.setText(parts[3]);
        playerGraphicsBoard = new GraphicsBoard(isPlayerBlack ? "black" : "white");
        enemyGraphicsBoard = new GraphicsBoard(isPlayerBlack ? "white" : "black");

        // Set the turn logic: White always starts, so check if player is white or black
        isPlayerTurn = !isPlayerBlack;
        if(!isPlayerTurn) {turnLabel.setText("Enemy turn");}

        // Set up the board and begin the game
        createChessBoard();
        // Add listener to the GridPane's scene property to ensure it's set before accessing the window
        Platform.runLater(() -> {
            Stage stage = (Stage) gridPane.getScene().getWindow();
            if (stage != null) {
                // Handle window close event
                stage.setOnCloseRequest(this::handleWindowClose);
            } else {
                System.out.println("Stage is not yet available.");
            }
        });
        //timer
        startTimer();
    }

    public void handleServerMessage(String message)
    {
        System.out.println("message for debug: " + message); // remove later
        String[] messageParts = message.split("\\|");

        if (messageParts.length <= 1)
        {
            System.out.println("information message or an error: " + message);
        }
        else if(messageParts[0].equals("draw_offer")) {
            showDrawOfferPopup();
        }
        else if (messageParts[0].equals("draw_offer_accepted")) {
            handleDrawAccepted();
        }
        else if(messageParts[0].equals("draw_offer_rejected")) {
            handleDrawRejected();
        }
        else if(messageParts[0].equals("enemy_resigned")) {
            handleEnemyResigned();
        }
        else
        {
            // if it's a normal move message
            setCurrentEnemyMoveMessage(messageParts[1]);
            fetchEnemyMove();
        }
    }

    @Override
    protected void onTileClicked(int row, int col) {
        if (isPlayerTurn && isLegalMove(selectedRow, selectedCol, row, col)) {
            // Perform the move
            makePlayerMove(selectedRow, selectedCol, row, col);
            pieceSelected = false;
            clearHighlights();
            isPlayerTurn = false;
            turnLabel.setText("Enemy turn");

            // Send move to server
            String move = getMoveUCI(selectedRow, selectedCol, row, col);
            try
            {
                String message = gameId + "|" + move;
                session.getBasicRemote().sendText(message);
                if(gameEnded) {
                    sendResult(gameResult);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            // If the user clicks an invalid move, just clear the selection and highlights
            clearHighlights();
            pieceSelected = false;
        }
    }

    @Override
    protected void fetchEnemyMove()
    {
        String from = currentEnemyMoveMessage.substring(0, 2);
        String to = currentEnemyMoveMessage.substring(2, 4);

        Square fromSquare = Square.fromValue(from.toUpperCase());
        Square toSquare = Square.fromValue(to.toUpperCase());

        Move move = new Move(fromSquare, toSquare);
        // check if it's a promotion and change move accordingly
        if (currentEnemyMoveMessage.length() == 5)
        {
            Side side = isPlayerBlack ? Side.WHITE : Side.BLACK;
            Piece promotionPiece = getPromotionPiece(currentEnemyMoveMessage.charAt(4), side); // Get the promotion piece
            move = new Move(fromSquare, toSquare, promotionPiece);
        }

        if (chesslibBoard.isMoveLegal(move, true))
        {
            moveEnemyPiece(getRowFromUci(fromSquare.value()), getColFromUci(fromSquare.value()),
                    getRowFromUci(toSquare.value()), getColFromUci(toSquare.value()), move);
            chesslibBoard.doMove(move);

            // check if a mate or a draw has occurred, if yes then go to end screen
            checkForDrawOrMateAndGotoEndScreen();

            isPlayerTurn = true; // Player's turn
            turnLabel.setText("Your turn");
        } else {
            System.out.println("Illegal move by online foe: " + currentEnemyMoveMessage + " \nProbably a bug...");
        }
    }

    @Override
    protected void handleResignAction(ActionEvent actionEvent)
    {
        stopTimer();
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resign Confirmation");
        alert.setHeaderText("Are you sure you want to resign?");
        alert.setContentText("You will forfeit the game.");

        // Add Yes and No buttons to the dialog
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton);

        // Show the dialog and capture the user's response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton)
        {
            // if yes is clicked
            try
            {
                gameResult = isPlayerBlack ? "white_win" : "black_win";
                String resignationMessage = "resignation|" + gameResult + "|" + gameId;
                session.getBasicRemote().sendText(resignationMessage);
                System.out.println("You resigned and the game has ended in your loss");

                // logic after resignation
                gameEnded = true;
                showEndGameScreen("You resigned...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void goToHomeScreen()
    {
        try
        {
            // close the session when exiting to home screen
            if (session != null && session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "User quit the game"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            // Load the FXML file for the home screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/homeScreen.fxml"));
            Parent homeScreen = loader.load();

            // set the username
            HomeScreenController waitingRoomController = loader.getController();
            waitingRoomController.setUsername(username);

            // Get the current scene and set the new root
            Stage stage = (Stage) gridPane.getScene().getWindow();
            // cancel the "x" button listener, as we are not going to be in an online game anymore
            if (stage != null)
            {
                // Remove the "X" button listener by setting it to null
                stage.setOnCloseRequest(null);
                stage.setScene(new Scene(homeScreen));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEnemyResigned()
    {
        stopTimer();
        gameEnded = true;
        gameResult = isPlayerBlack ? "black_win" : "white_win";
        showEndGameScreen("You won! " + enemyLabel.getText() + " has resigned...");
    }

    @Override
    protected void restartGame()
    {
        return;
    }

    @FXML
    private void onDrawOffer()
    {
        if (!canOfferDraw) {
            showCooldownAlert();
            return;
        }

        // Send draw offer to the opponent
        try {
            String drawOfferMessage = "draw_offer|" + gameId;
            session.getBasicRemote().sendText(drawOfferMessage);
            System.out.println("Draw offer sent to opponent");

            // Set cooldown and disable draw offer for 1 minute
            canOfferDraw = false;

            // Start a cooldown timer of 60 seconds
            startDrawCooldownTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDrawCooldownTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                canOfferDraw = true;
            }
        }, 60 * 1000);  // 1-minute cooldown
    }

    private void showCooldownAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cooldown");
            alert.setHeaderText(null);
            alert.setContentText("You can only offer a draw every 1 minute.");
            alert.showAndWait();
        });
    }

    private void showDrawOfferPopup() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Draw Offer");
            alert.setHeaderText("Your opponent has offered a draw.");
            alert.setContentText("Do you accept the draw offer?");

            ButtonType acceptButton = new ButtonType("Accept");
            ButtonType rejectButton = new ButtonType("Reject", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(acceptButton, rejectButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == acceptButton) {
                acceptDrawOffer();
            } else {
                rejectDrawOffer();
            }
        });
    }

    private void acceptDrawOffer() {
        try {
            stopTimer();
            String drawAcceptedMessage = "draw_offer_accepted|" + gameId;
            session.getBasicRemote().sendText(drawAcceptedMessage);
            System.out.println("Draw offer accepted and game ended as draw");
            handleDrawAccepted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rejectDrawOffer() {
        try {
            String rejectMessage = "draw_offer_rejected|" + gameId;
            session.getBasicRemote().sendText(rejectMessage);
            System.out.println("Draw offer rejected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDrawAccepted()
    {
        stopTimer();
        gameEnded = true;
        gameResult = "draw";
        showEndGameScreen("draw offer accepted! It's a draw.");
    }

    private void handleDrawRejected()
    {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Draw Rejected");
            alert.setHeaderText(null);
            alert.setContentText(enemyLabel.getText() + " has rejected your draw offer.");
            alert.showAndWait();
        });
    }


    private void sendResult(String gameResult)
    {
        try {
            String resultMessage = "game_over|" + gameResult + "|" + gameId;
            session.getBasicRemote().sendText(resultMessage);
            System.out.println("gameEnded message sent: " + resultMessage);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getMoveUCI(int fromRow, int fromCol, int toRow, int toCol) {
        // Convert to UCI format (e.g., "e2e4")
        String from = getColLetter(fromCol) + (8 - fromRow);
        String to = getColLetter(toCol) + (8 - toRow);
        // check for pawn promotion to add to the UCI move representation
        String UCIMove = from + to;
        if(playerPromotionValue != null)
        {
            UCIMove += playerPromotionValue;
            playerPromotionValue = null;
        }
        return UCIMove;
    }

    // Method to handle window close event
    private void handleWindowClose(WindowEvent event) {
        stopTimer();
        // Example: Ask for confirmation before closing
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("You're about to exit the game.");
        alert.setContentText("Are you sure you want to exit? You will automatically lose this game.");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // Close the session and clean up resources before exiting
            try {
                // game ending logic sent to the server
                gameResult = isPlayerBlack ? "white_win" : "black_win";
                String resignationMessage = "resignation|" + gameResult + "|" + gameId;
                session.getBasicRemote().sendText(resignationMessage);

                // close the session and the app
                session.close();
                Platform.exit();  // Close the application
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Prevent the window from closing
            event.consume();
        }
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setCurrentEnemyMoveMessage(String message) {
        this.currentEnemyMoveMessage = message;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    private void startTimer() {
        // Initialize the start time
        startTimeMillis = System.currentTimeMillis();

        // Set up the timeline to update the elapsed time every second
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
            timerLabelOnline.setText("Time:\n" + elapsedSeconds + " seconds");
        }));
        timer.setCycleCount(Timeline.INDEFINITE);  // Keep the timer running
        timer.play();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }
}

