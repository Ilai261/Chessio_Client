// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class is handling a big part of the logic behind the chess game, that is specific to the bot
// it uses stockfish api to get the best move at different grades of thinking

package org.chessio.chessio_client.SceneControllers;

import com.github.bhlangonijr.chesslib.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.chessio.chessio_client.Models.GraphicsBoard;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.*;
import java.io.File;
import java.util.Optional;

public class BotBoardController extends BaseBoardController
{

    private PrintWriter stockfishWriter;
    private BufferedReader stockfishReader;
    @FXML
    private Label timerLabel;

    private Timeline timer;  // to update the timer every second
    private long startTimeMillis;  // tracks the start time


    @Override
    public void initializeGame(String playerColor)
    {
        isPlayerBlack = playerColor.equals("black");
        playerGraphicsBoard = new GraphicsBoard(playerColor);
        enemyGraphicsBoard = new GraphicsBoard(isPlayerBlack ? "white" : "black");

        // so check if player is white or black
        isPlayerTurn = !isPlayerBlack; // Player's turn if they are white
        createChessBoard();//creates the logic behind
        startStockfish();//starts stock fish
        startTimer();//the timer of seconds
        setStockfishLevel(this.enemyLevel);  // Set Stockfish level
        if (!isPlayerTurn)
        {
            turnLabel.setText("Enemy turn");
            fetchEnemyMove();
        }
    }


    // handles tile click event for moving pieces

    @Override
    protected void onTileClicked(int row, int col) {
        if (isLegalMove(selectedRow, selectedCol, row, col))
        {
            makePlayerMove(selectedRow, selectedCol, row, col); // Perform move
            pieceSelected = false; // Deselect after move
            clearHighlights(); // Clear highlights after the move
            // Switch turns after a valid move
            isPlayerTurn = false;
            turnLabel.setText("Enemy turn");
            fetchEnemyMove(); // Stockfish makes a move
        }
        else
        {
            // If the user clicks an invalid move, it just clear the selection and highlight.
            clearHighlights();
            pieceSelected = false;
        }
    }
    @Override
    protected void fetchEnemyMove()
    {
        if(gameEnded) {
            stopTimer();
            return; }

        String bestMove = getBestMoveFromStockfish();
        if (bestMove != null) {
            Square from = Square.fromValue(bestMove.substring(0, 2).toUpperCase());
            Square to = Square.fromValue(bestMove.substring(2, 4).toUpperCase());
            Move move = new Move(from, to);

            // check if it's a promotion and change move accordingly
            if (bestMove.length() == 5)
            {
                Side side = isPlayerBlack ? Side.WHITE : Side.BLACK;
                Piece promotionPiece = getPromotionPiece(bestMove.charAt(4), side); // get the promotion piece
                move = new Move(from, to, promotionPiece);
            }

            if (chesslibBoard.isMoveLegal(move, true))
            {
                moveEnemyPiece(getRowFromUci(from.value()), getColFromUci(from.value()),
                        getRowFromUci(to.value()), getColFromUci(to.value()), move);
                chesslibBoard.doMove(move);

                // checks if a mate or a draw has occurred, if yes then go to end screen
                checkForDrawOrMateAndGotoEndScreen();

                isPlayerTurn = true; // Player's turn
                turnLabel.setText("Your turn");
            } else {
                System.out.println("Illegal move by bot: " + bestMove + " \nProbably a bug...");
            }
        }
    }

    @FXML
    @Override
    public void handleResignAction(ActionEvent actionEvent)
    {
        stopTimer();
        // Creates a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resign Confirmation");
        alert.setHeaderText("Are you sure you want to resign?");
        alert.setContentText("You will forfeit the game.");

        //Yes and No buttons to the dialog
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton);

        // show the dialog and capture the user's response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // If "Yes" is clicked, show end game screen
            showEndGameScreen("You resigned...");
            gameEnded = true;
        }
    }

    @Override
    protected void goToHomeScreen() {
        try {
            // loads the FXML file for the home screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/homeScreen.fxml"));
            Parent homeScreen = loader.load();

            // sets the username
            HomeScreenController waitingRoomController = loader.getController();
            waitingRoomController.setUsername(username);

            // gets the current scene and set the new root
            Stage stage = (Stage) gridPane.getScene().getWindow(); // Assuming the gridPane is already part of the scene
            stage.setScene(new Scene(homeScreen));
            stage.show();
        } catch (IOException e) {
            System.out.println("An error occurred while performing the operation:" + " changing to goToHomeScreen");
        }
    }

    // method to restart the game
    @FXML
    @Override
    protected void restartGame()
    {
        chesslibBoard = new com.github.bhlangonijr.chesslib.Board(); // Reset the chessboard
        gameEnded = false;
        initializeGame(isPlayerBlack ? "black" : "white"); // Reinitialize the game

        // hides the end-game overlay
        endGameOverlay.setVisible(false);
        // Clears the dimming effect on the chessboard
        gridPane.setEffect(null);
    }

    private void startStockfish() {
        try {
            // load Stockfish from the resources folder
            InputStream stockfishStream = getClass().getResourceAsStream("/stockfish.exe");

            if (stockfishStream == null) {
                throw new IllegalStateException("Stockfish executable not found in resources!");
            }

            // create a temporary file for Stockfish executable
            File tempFile = File.createTempFile("stockfish", ".exe");
            tempFile.deleteOnExit(); // Make sure to delete after the process exits

            // write the Stockfish executable to the temporary file
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stockfishStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Starts Stockfish process using the extracted temporary file
            Process stockfishProcess = new ProcessBuilder(tempFile.getAbsolutePath()).start();
            stockfishWriter = new PrintWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()), true);
            stockfishReader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));

            // Initializes the usage of UCI protocol
            stockfishWriter.println("uci");
            stockfishWriter.println("isready");
            stockfishReader.readLine(); // Wait for "readyok"
            stockfishWriter.println("ucinewgame");

        } catch (Exception e) {
            System.out.println("An error occurred while performing the operation:" + " starting StockFish exec");
        }
    }

    // set the difficulty level for the Stockfish
    private void setStockfishLevel(int level) {
        stockfishWriter.println("setoption name Skill Level value " + level);
    }

    // sends the current board state to Stockfish and get the best move
    private String getBestMoveFromStockfish() {
        try {
            String fen = chesslibBoard.getFen();
            stockfishWriter.println("position fen " + fen);
            stockfishWriter.println("go movetime 1000"); // Think for 1 second
            String line;
            while ((line = stockfishReader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    return line.split(" ")[1]; // Best move is after "bestmove"
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred while performing the operation:" + " getting the best move");
        }
        return null;
    }

    private void startTimer() {
        // initialize the start time
        startTimeMillis = System.currentTimeMillis();

        // Sets up the timeline to update the elapsed time every second
        timer = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
            long elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
            timerLabel.setText("Time:\n" + elapsedSeconds + " seconds");
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
