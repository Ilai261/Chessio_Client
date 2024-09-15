package org.chessio.chessio_client.SceneControllers;

import com.github.bhlangonijr.chesslib.Piece;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.chessio.chessio_client.Models.GraphicsBoard;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.Board;

import java.io.*;
import java.util.List;

public class BotBoardController {

    @FXML
    private GridPane gridPane; // Reference to the GridPane from the FXML

    @FXML
    private Button resignButton; // Resign button

    @FXML
    private ImageView resignIcon; // Resign button icon

    @FXML
    private Label turnLabel; // Label for "Your turn" (optional)

    @FXML
    private Label usernameLabel; // For the "Username" label

    private static final int TILE_SIZE = 80; // Size of each tile
    private static final int BOARD_SIZE = 8; // Chessboard is 8x8

    private GraphicsBoard playerGraphicsBoard;
    private GraphicsBoard enemyGraphicsBoard;
    private boolean isPlayerBlack;
    private boolean pieceSelected = false;
    private String selectedPiece = null;
    private int selectedRow = -1, selectedCol = -1;
    private boolean isPlayerTurn;  // Track if it's the player's turn

    private Board chesslibBoard = new Board();
    private Process stockfishProcess;
    private PrintWriter stockfishWriter;
    private BufferedReader stockfishReader;

    // Initialize the game and start Stockfish process
    public void initializeGame(String playerColor, int enemyLevel) {
        isPlayerBlack = playerColor.equals("black");
        playerGraphicsBoard = new GraphicsBoard(playerColor);
        enemyGraphicsBoard = new GraphicsBoard(isPlayerBlack ? "white" : "black");

        // Set the turn logic: White always starts, so check if player is white or black
        isPlayerTurn = !isPlayerBlack; // Player's turn if they are white

        createChessBoard();
        startStockfish();
        setStockfishLevel(enemyLevel);  // Set Stockfish level
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
            stockfishProcess = new ProcessBuilder(tempFile.getAbsolutePath()).start();
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

    // Handle Stockfish's move after the player's turn
    private void makeEnemyMove() {
        String bestMove = getBestMoveFromStockfish();
        if (bestMove != null) {
            Square from = Square.fromValue(bestMove.substring(0, 2).toUpperCase());
            Square to = Square.fromValue(bestMove.substring(2, 4).toUpperCase());
            Move move = new Move(from, to);

            if (chesslibBoard.isMoveLegal(move, true)) {
                chesslibBoard.doMove(move);
                handlePostMoveUpdate(getRowFromUci(from.value()), getColFromUci(from.value()),
                        getRowFromUci(to.value()), getColFromUci(to.value()));
            }
        }
        isPlayerTurn = true; // Player's turn
    }

    private void handlePostMoveUpdate(int fromRow, int fromCol, int toRow, int toCol) {
        // Determine if the move is for the player or the enemy
        String pieceSymbol = isPlayerTurn ? playerGraphicsBoard.getPieceAt(fromRow, fromCol) : enemyGraphicsBoard.getPieceAt(fromRow, fromCol);

        // Remove any piece at the destination from the opponent's board
        if (isPlayerTurn) {
            // Player capturing an enemy piece
            if (enemyGraphicsBoard.getPieceAt(toRow, toCol) != null) {
                enemyGraphicsBoard.removePieceAt(toRow, toCol);
            }
        } else {
            // Enemy capturing a player piece
            if (playerGraphicsBoard.getPieceAt(toRow, toCol) != null) {
                playerGraphicsBoard.removePieceAt(toRow, toCol);
            }
        }

        // Normal move: move the piece to the new position
        if (isPlayerTurn) {
            playerGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
            playerGraphicsBoard.removePieceAt(fromRow, fromCol);
        } else {
            enemyGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
            enemyGraphicsBoard.removePieceAt(fromRow, fromCol);
        }


        // Update the grid after the move
        gridPane.getChildren().clear();
        createChessBoard();

        // Toggle the turn after a valid move
        isPlayerTurn = !isPlayerTurn;
        System.out.println(isPlayerTurn ? "Player's turn" : "Opponent's turn");
    }


    // Method to create the chessboard with pieces
    private void createChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Create the tile
                Rectangle tile = createTile(row, col);
                gridPane.add(tile, col, row);

                int displayRow = isPlayerBlack ? BOARD_SIZE - 1 - row : row;
                String pieceSymbol = playerGraphicsBoard.getPieceAt(displayRow, col);
                if (pieceSymbol == null) {
                    pieceSymbol = enemyGraphicsBoard.getPieceAt(displayRow, col);
                }

                // Declare final variables to avoid issues with lambda expressions
                final int finalRow = row;
                final int finalCol = col;
                final String finalPieceSymbol = pieceSymbol;

                // Attach click events for both the tile and the piece
                if (pieceSymbol != null) {
                    ImageView piece = createPiece(pieceSymbol);
                    gridPane.add(piece, col, row);

                    // Piece click event
                    piece.setOnMouseClicked(e -> handleTileOrPieceClick(finalRow, finalCol, finalPieceSymbol));
                }

                // Tile click event
                tile.setOnMouseClicked(e -> handleTileOrPieceClick(finalRow, finalCol, finalPieceSymbol));
            }
        }
    }


    // Handle both piece and tile clicks in a unified way
    private void handleTileOrPieceClick(int row, int col, String pieceSymbol) {
        if (pieceSelected) {
            // If a piece is already selected, we are attempting to move it
            onTileClicked(row, col);
        } else {
            // If no piece is selected, we are attempting to select a piece
            if (pieceSymbol != null) {
                onPieceClicked(row, col, pieceSymbol);
            }
        }
    }


    // Method to create a chessboard tile
    private Rectangle createTile(int row, int col) {
        Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
        tile.setFill((row + col) % 2 == 0 ? Color.rgb(245, 245, 245) : Color.rgb(118, 150, 86));
        tile.setOnMouseClicked(e -> onTileClicked(row, col));
        return tile;
    }

    // Handle piece click event
    private void onPieceClicked(int row, int col, String pieceSymbol) {
        // Ensure it's the player's turn before allowing them to move
        if (!isPlayerTurn) {
            System.out.println("It's not your turn!");
            return;
        }
        // If a piece was already selected, deselect it and remove highlights
        if (pieceSelected) {
            clearHighlights(); // Remove previous highlights
            if (selectedRow == row && selectedCol == col) {
                // Same piece clicked again, cancel the selection
                pieceSelected = false;
                selectedPiece = null;
                return;
            }
        }
        // Select the new piece and highlight its possible moves
        selectedPiece = pieceSymbol;
        selectedRow = row;
        selectedCol = col;
        highlightTile(row, col); // Highlight the selected tile
        highlightLegalMoves(row, col); // Highlight possible moves
        pieceSelected = true;
    }




    // Handle tile click event for moving pieces
    private void onTileClicked(int row, int col) {
        if (pieceSelected && isLegalMove(selectedRow, selectedCol, row, col)) {
            movePiece(selectedRow, selectedCol, row, col); // Perform move
            pieceSelected = false; // Deselect after move
            clearHighlights(); // Clear highlights after the move
            // Switch turns after a valid move
            isPlayerTurn = false;
            makeEnemyMove(); // Stockfish makes a move
        } else if (pieceSelected) {
            // If the user clicks an invalid move, just clear the selection and highlights
            clearHighlights();
            pieceSelected = false;
        }
    }

    // Method to clear all highlighted tiles
    private void clearHighlights() {
        gridPane.getChildren().clear(); // Clear the grid
        createChessBoard(); // Recreate the board without any highlights
    }

    // Highlight legal moves for a selected piece
    private void highlightLegalMoves(int row, int col) {
        Square square = getSquare(row, col);
        try {
            // Generate all legal moves from the current board state
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(chesslibBoard);

            // Filter moves that are specific to the given square
            for (Move move : legalMoves) {
                if (move.getFrom().equals(square)) {
                    int toRow = getRowFromUci(move.getTo().value());
                    int toCol = getColFromUci(move.getTo().value());
                    highlightTile(toRow, toCol); // Highlight legal moves without hiding pieces
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Method to highlight tiles where the piece can move or is selected
    private void highlightTile(int row, int col) {
        // Ensure row and col are within valid bounds
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            for (javafx.scene.Node node : gridPane.getChildren()) {
                if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                    if (node instanceof Rectangle) {
                        Rectangle tile = (Rectangle) node;
                        // Darken the existing background color of the tile
                        Color originalColor = (Color) tile.getFill();
                        Color highlightedColor = originalColor.darker(); // Darken it slightly
                        tile.setFill(highlightedColor);
                    }
                }
            }
        } else {
            System.err.println("Invalid tile position: row=" + row + ", col=" + col);
        }
    }



    // Check if a move is legal
    private boolean isLegalMove(int fromRow, int fromCol, int toRow, int toCol) {
        Square from = getSquare(fromRow, fromCol);
        Square to = getSquare(toRow, toCol);
        try {
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(chesslibBoard);
            return legalMoves.stream().anyMatch(move -> move.getFrom().equals(from) && move.getTo().equals(to));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        System.out.println("Moving piece from " + fromRow + "," + fromCol + " to " + toRow + "," + toCol);
        Square from = getSquare(fromRow, fromCol);
        Square to = getSquare(toRow, toCol);

        // Move the piece in the chesslibBoard
        Move move = new Move(from, to);
        if (chesslibBoard.isMoveLegal(move, true)) {
            chesslibBoard.doMove(move);

            // Check if the destination tile contains an enemy piece
            String pieceSymbol = playerGraphicsBoard.getPieceAt(fromRow, fromCol);
            String targetPieceSymbol = enemyGraphicsBoard.getPieceAt(toRow, toCol);

            if (pieceSymbol != null) {
                // If the player's piece is moving, check for enemy capture
                if (targetPieceSymbol != null) {
                    // Capture the enemy piece
                    enemyGraphicsBoard.removePieceAt(toRow, toCol);
                }
                // Move player's piece to the destination
                playerGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
                playerGraphicsBoard.removePieceAt(fromRow, fromCol);
            } else {
                // Otherwise, it's the enemy's turn to move
                pieceSymbol = enemyGraphicsBoard.getPieceAt(fromRow, fromCol);
                String targetPlayerPiece = playerGraphicsBoard.getPieceAt(toRow, toCol);

                if (pieceSymbol != null) {
                    // If there's a player piece at the destination, capture it
                    if (targetPlayerPiece != null) {
                        playerGraphicsBoard.removePieceAt(toRow, toCol);
                    }
                    // Move enemy's piece to the destination
                    enemyGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
                    enemyGraphicsBoard.removePieceAt(fromRow, fromCol);
                }
            }

            // Clear and update the grid after the move
            gridPane.getChildren().clear(); // Clear the grid
            createChessBoard(); // Recreate the board with updated pieces

            // Switch the turn after a valid move
            isPlayerTurn = !isPlayerTurn; // Toggle the turn after a move

            System.out.println(isPlayerTurn ? "Player's turn" : "Opponent's turn");

            // Check if the game is over (checkmate or stalemate)
            if (chesslibBoard.isMated()) {
                System.out.println("Checkmate! Game over.");
                showEndGameScreen("Checkmate! You win!");
            } else if (chesslibBoard.isStaleMate()) {
                System.out.println("Stalemate! Game over.");
                showEndGameScreen("Stalemate! It's a draw.");
            }
        } else {
            System.out.println("Illegal move: " + from + " to " + to);
        }
    }







    private void showEndGameScreen(String message) {
        // Clear the current grid and display the end game message
        gridPane.getChildren().clear();

        Label endGameLabel = new Label(message);
        endGameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        gridPane.add(endGameLabel, 0, 0, BOARD_SIZE, BOARD_SIZE); // Span the label across the entire grid

        // You can add more logic here to offer a "Play Again" button or exit the game
        Button playAgainButton = new Button("Play Again");
        playAgainButton.setOnAction(e -> restartGame());
        gridPane.add(playAgainButton, BOARD_SIZE / 2, BOARD_SIZE / 2);
    }

    // Method to restart the game (optional, if needed)
    private void restartGame() {
        chesslibBoard = new com.github.bhlangonijr.chesslib.Board(); // Reset the chessboard
        initializeGame(isPlayerBlack ? "black" : "white", 1); // Reinitialize the game
    }




    // Utility methods for square conversions
    private Square getSquare(int row, int col) {
        // Convert Java row and col (0-based, top-left) to UCI coordinates (1-8, a-h).
        String squareName = (getColLetter(col) + (BOARD_SIZE - row)).toUpperCase();

        System.out.println("Square name: " + squareName + " row: " + row + " col: " + col);//row and col

        return Square.valueOf(squareName); // Ensure the square is in uppercase
    }

    private String getColLetter(int col) {
        return String.valueOf((char) ('a' + col));
    }

    private int getRowFromUci(String uci) {
        return BOARD_SIZE - Character.getNumericValue(uci.charAt(1)); // Correct row conversion
    }



    private int getColFromUci(String uci) {
        return uci.charAt(0) - 'A'; // 'a' to 'h' should map to 0 to 7
    }

    // Method to create an ImageView for a piece based on the piece symbol
    private ImageView createPiece(String pieceSymbol) {
        String color = pieceSymbol.substring(0, 1).equals("W") ? "white" : "black";
        String pieceName = switch (pieceSymbol.substring(1)) {
            case "P" -> "pawn";
            case "R" -> "rook";
            case "N" -> "knight";
            case "B" -> "bishop";
            case "Q" -> "queen";
            case "K" -> "king";
            default -> null;
        };

        if (pieceName != null) {
            String imagePath = "/org/chessio/chessio_client/Icons/" + color + "_" + pieceName + ".png";
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (!image.isError()) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(TILE_SIZE * 0.8); // Adjust image size to fit the tile
                imageView.setFitHeight(TILE_SIZE * 0.8);
                return imageView;
            } else {
                System.err.println("Error loading image: " + imagePath);
            }
        }
        return null;
    }
}