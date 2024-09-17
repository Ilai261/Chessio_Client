package org.chessio.chessio_client.SceneControllers;

import com.github.bhlangonijr.chesslib.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chessio.chessio_client.Models.GraphicsBoard;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.*;
import java.io.File;
import java.util.Optional;

public class BotBoardController extends BaseBoardController
{

    private PrintWriter stockfishWriter;
    private BufferedReader stockfishReader;

    // Initialize the game and start Stockfish process
    @Override
    public void initializeGame(String playerColor)
    {
        isPlayerBlack = playerColor.equals("black");
        playerGraphicsBoard = new GraphicsBoard(playerColor);
        enemyGraphicsBoard = new GraphicsBoard(isPlayerBlack ? "white" : "black");

        // Set the turn logic: White always starts, so check if player is white or black
        isPlayerTurn = !isPlayerBlack; // Player's turn if they are white
        createChessBoard();
        startStockfish();
        setStockfishLevel(this.enemyLevel);  // Set Stockfish level
        if (!isPlayerTurn)
        {
            fetchEnemyMove();
        }
    }


    // Handle tile click event for moving pieces

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
            // If the user clicks an invalid move, just clear the selection and highlights
            clearHighlights();
            pieceSelected = false;
        }
    }
    @Override
    protected void fetchEnemyMove()
    {
        if(gameEnded) { return; }

        String bestMove = getBestMoveFromStockfish();
        if (bestMove != null) {
            Square from = Square.fromValue(bestMove.substring(0, 2).toUpperCase());
            Square to = Square.fromValue(bestMove.substring(2, 4).toUpperCase());
            Move move = new Move(from, to);

            // check if it's a promotion and change move accordingly
            if (bestMove.length() == 5)
            {
                Side side = isPlayerBlack ? Side.WHITE : Side.BLACK;
                Piece promotionPiece = getPromotionPiece(bestMove.charAt(4), side); // Get the promotion piece
                move = new Move(from, to, promotionPiece);
            }

            if (chesslibBoard.isMoveLegal(move, true))
            {
                moveEnemyPiece(getRowFromUci(from.value()), getColFromUci(from.value()),
                        getRowFromUci(to.value()), getColFromUci(to.value()), move);
                chesslibBoard.doMove(move);

                // check if a mate or a draw has occurred, if yes then go to end screen
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
        if (result.isPresent() && result.get() == yesButton) {
            // If "Yes" is clicked, show end game screen
            showEndGameScreen("You resigned...");
            gameEnded = true;
        }
    }

    private void startStockfish() {
        try {
            // Load Stockfish from the resources folder
            // The path should be 'stockfish/stockfish.exe' or the relative path inside the resources folder.
            InputStream stockfishStream = getClass().getResourceAsStream("/stockfish.exe");

            if (stockfishStream == null) {
                throw new IllegalStateException("Stockfish executable not found in resources!");
            }

            // Create a temporary file for Stockfish executable
            File tempFile = File.createTempFile("stockfish", ".exe");
            tempFile.deleteOnExit(); // Make sure to delete after the process exits

            // Write the Stockfish executable to the temporary file
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stockfishStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Start Stockfish process using the extracted temporary file
            Process stockfishProcess = new ProcessBuilder(tempFile.getAbsolutePath()).start();
            stockfishWriter = new PrintWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()), true);
            stockfishReader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));

            // Initialize UCI protocol
            stockfishWriter.println("uci");
            stockfishWriter.println("isready");
            stockfishReader.readLine(); // Wait for "readyok"
            stockfishWriter.println("ucinewgame");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set the difficulty level for Stockfish
    private void setStockfishLevel(int level) {
        stockfishWriter.println("setoption name Skill Level value " + level);
    }

    // Send the current board state to Stockfish and get the best move
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
            e.printStackTrace();
        }
        return null;
    }
}