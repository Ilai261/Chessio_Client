package org.chessio.chessio_client.SceneControllers;

import com.github.bhlangonijr.chesslib.*;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.chessio.chessio_client.Models.GraphicsBoard;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;

import java.io.*;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

    @FXML
    private VBox endGameOverlay;

    @FXML
    private Label endGameLabel;


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
        if (!isPlayerTurn)
        {
            makeEnemyMove();
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
                moveEnemyPiece(getRowFromUci(from.value()), getColFromUci(from.value()),
                        getRowFromUci(to.value()), getColFromUci(to.value()), move);
                chesslibBoard.doMove(move);
                // Check for checkmate or stalemate
                if (chesslibBoard.isMated()) {
                    System.out.println("Checkmate! Game over.");
                    showEndGameScreen("Checkmate! You lost...");
                } else if (chesslibBoard.isStaleMate()) {
                    System.out.println("Stalemate! Game over.");
                    showEndGameScreen("Stalemate! It's a draw.");
                }
            }
        }
        isPlayerTurn = true; // Player's turn
        turnLabel.setText("Your turn");
    }

    private void moveEnemyPiece(int fromRow, int fromCol, int toRow, int toCol, Move move) {
        // Determine if the move is for the player or the enemy
        String pieceSymbol = isPlayerTurn ? playerGraphicsBoard.getPieceAt(fromRow, fromCol) : enemyGraphicsBoard.getPieceAt(fromRow, fromCol);

        // Handle castling
        Piece movingPiece = chesslibBoard.getPiece(move.getFrom());
        if (movingPiece.getPieceType() == PieceType.KING && Math.abs(fromCol - toCol) == 2) {
            makeCastling(fromRow, toCol, enemyGraphicsBoard);
        }
        // Handle en passant
        else if (movingPiece.getPieceType() == PieceType.PAWN && chesslibBoard.getEnPassantTarget() != Square.NONE
                && move.getTo().equals(chesslibBoard.getEnPassantTarget())) {
            int captureRow = isPlayerBlack ? toRow + 1 : toRow - 1;
            enemyGraphicsBoard.removePieceAt(captureRow, toCol); // Remove captured pawn
        }
        else if (movingPiece.getPieceType() == PieceType.PAWN && (toRow == 0 || toRow == BOARD_SIZE - 1))
        {
            // Stockfish will automatically handle the promotion piece in the UCI
            Piece promotionPiece = chesslibBoard.getPiece(move.getTo()); // Get the promotion piece
            pieceSymbol = getPieceSymbolForPromotion(promotionPiece);
            enemyGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
            enemyGraphicsBoard.removePieceAt(fromRow, fromCol);
        }

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

    private void makeCastling(int fromRow, int toCol, GraphicsBoard graphicsBoard) {
        if (toCol == 6) { // Kingside castling
            graphicsBoard.setPieceAt(fromRow, 5, graphicsBoard.getPieceAt(fromRow, 7)); // Move rook
            graphicsBoard.removePieceAt(fromRow, 7);
        } else if (toCol == 2) { // Queenside castling
            graphicsBoard.setPieceAt(fromRow, 3, graphicsBoard.getPieceAt(fromRow, 0)); // Move rook
            graphicsBoard.removePieceAt(fromRow, 0);
        }
    }


    private void createChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Flip the row if the player is black
                int displayRow = isPlayerBlack ? BOARD_SIZE - 1 - row : row; // may change this

                Rectangle tile = createTile(displayRow, col);
                gridPane.add(tile, col, row);

                String pieceSymbol = playerGraphicsBoard.getPieceAt(displayRow, col);
                if (pieceSymbol == null) {
                    pieceSymbol = enemyGraphicsBoard.getPieceAt(displayRow, col);
                }

                if (pieceSymbol != null) {
                    ImageView piece = createPiece(pieceSymbol);
                    gridPane.add(piece, col, row);

                    int finalCol = col;
                    String finalPieceSymbol = pieceSymbol;
                    piece.setOnMouseClicked(e -> handleTileOrPieceClick(displayRow, finalCol, finalPieceSymbol));
                }

                int finalCol1 = col;
                String finalPieceSymbol1 = pieceSymbol;
                tile.setOnMouseClicked(e -> handleTileOrPieceClick(displayRow, finalCol1, finalPieceSymbol1));
            }
        }
    }


    // Handle both piece and tile clicks in a unified way
    private void handleTileOrPieceClick(int row, int col, String pieceSymbol)
    {

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
            movePlayerPiece(selectedRow, selectedCol, row, col); // Perform move
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

    private void highlightLegalMoves(int row, int col) {
        // Adjust row for black's perspective
        int adjustedRow = isPlayerBlack ? BOARD_SIZE - 1 - row : row;
        int adjustedCol = col; // We do not need to flip columns, only rows for black

        Square square = getSquare(adjustedRow, adjustedCol);
        try {
            // Generate all legal moves from the current board state
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(chesslibBoard);

            // Filter moves that are specific to the given square
            for (Move move : legalMoves) {
                if (move.getFrom().equals(square)) {
                    int toRow = getRowFromUci(move.getTo().value());
                    int toCol = getColFromUci(move.getTo().value());

                    // Adjust the target row for black's perspective
                    //int adjustedToRow = isPlayerBlack ? BOARD_SIZE - 1 - toRow : toRow;

                    highlightTile(toRow, toCol); // Highlight legal moves
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void highlightTile(int row, int col) {
        // Adjust row for black's perspective
        int adjustedRow = isPlayerBlack ? BOARD_SIZE - 1 - row : row;

        // Ensure adjustedRow and col are within valid bounds
        if (adjustedRow >= 0 && adjustedRow < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            for (javafx.scene.Node node : gridPane.getChildren()) {
                if (GridPane.getRowIndex(node) == adjustedRow && GridPane.getColumnIndex(node) == col) {
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
        int adjustedFromRow = isPlayerBlack ? BOARD_SIZE - 1 - fromRow : fromRow;
        int adjustedToRow = isPlayerBlack ? BOARD_SIZE - 1 - toRow : toRow;

        Square from = getSquare(adjustedFromRow, fromCol);
        Square to = getSquare(adjustedToRow, toCol);
        try {
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(chesslibBoard);
            return legalMoves.stream().anyMatch(move -> move.getFrom().equals(from) && move.getTo().equals(to));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void movePlayerPiece(int fromRow, int fromCol, int toRow, int toCol) {
        // Adjust the row and column based on the player's color
        int adjustedFromRow = isPlayerBlack ? BOARD_SIZE - 1 - fromRow : fromRow;
        int adjustedToRow = isPlayerBlack ? BOARD_SIZE - 1 - toRow : toRow;

        System.out.println("Moving piece from " + fromRow + "," + fromCol + " to " + toRow + "," + toCol);

        Square from = getSquare(adjustedFromRow, fromCol);
        Square to = getSquare(adjustedToRow, toCol);

        // Move the piece in the chesslibBoard
        AtomicReference<Move> move = new AtomicReference<>(new Move(from, to));

        // Check if the move is castling
        Piece movingPiece = chesslibBoard.getPiece(from);
        if (movingPiece.getPieceType() == PieceType.KING && Math.abs(fromCol - toCol) == 2) {
            makeCastling(fromRow, toCol, playerGraphicsBoard);
        }
        // Handle en passant
        else if (movingPiece.getPieceType() == PieceType.PAWN && chesslibBoard.getEnPassantTarget() != Square.NONE
                && to.equals(chesslibBoard.getEnPassantTarget())) {
            int captureRow = isPlayerBlack ? toRow + 1 : toRow - 1;
            enemyGraphicsBoard.removePieceAt(captureRow, toCol); // Remove captured pawn
        }
        else if (movingPiece.getPieceType() == PieceType.PAWN && (toRow == 0 || toRow == BOARD_SIZE - 1)) {
            // Trigger pawn promotion popup
            showPromotionPopup((selectedPromotionPiece) -> {
            // After the player selects a promotion piece, proceed with promotion
            Move promotionMove = new Move(from, to, selectedPromotionPiece);
            String pieceSymbol = getPieceSymbolForPromotion(selectedPromotionPiece);
            playerGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
            playerGraphicsBoard.removePieceAt(fromRow, fromCol);
            move.set(promotionMove);
            });
        }
        // now make the move itself
        if (chesslibBoard.isMoveLegal(move.get(), true)) {
            chesslibBoard.doMove(move.get());

            // Check if the destination tile contains an enemy piece
            String pieceSymbol = playerGraphicsBoard.getPieceAt(fromRow, fromCol);
            String targetPieceSymbol = enemyGraphicsBoard.getPieceAt(toRow, toCol);

            if (pieceSymbol != null) {
                // Player capturing an enemy piece
                if (targetPieceSymbol != null) {
                    enemyGraphicsBoard.removePieceAt(toRow, toCol);
                }
                playerGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
                playerGraphicsBoard.removePieceAt(fromRow, fromCol);
            } else {
                // Enemy capturing a player piece
                pieceSymbol = enemyGraphicsBoard.getPieceAt(fromRow, fromCol);
                String targetPlayerPiece = playerGraphicsBoard.getPieceAt(toRow, toCol);

                if (pieceSymbol != null) {
                    if (targetPlayerPiece != null) {
                        playerGraphicsBoard.removePieceAt(toRow, toCol);
                    }
                    enemyGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
                    enemyGraphicsBoard.removePieceAt(fromRow, fromCol);
                }
            }


            gridPane.getChildren().clear(); // Clear the grid
            createChessBoard(); // Recreate the board with updated pieces

            // Switch the turn after a valid move
            isPlayerTurn = !isPlayerTurn;
            turnLabel.setText("Enemy turn");
            System.out.println(isPlayerTurn ? "Player's turn" : "Opponent's turn");

            // Check for checkmate or stalemate
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



    private Square getSquare(int row, int col) {
        // Flip the row when the player is black, so coordinates are correctly adjusted
        int adjustedRow = isPlayerBlack ? BOARD_SIZE - 1 - row : row;
        String squareName = (getColLetter(col) + (BOARD_SIZE - adjustedRow)).toUpperCase(); //may need to change here cancel board size

        System.out.println("Square name: " + squareName + " row: " + row + " col: " + col);

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
        String color = pieceSymbol.charAt(0) == 'W' ? "white" : "black";
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
    private void showPromotionPopup(Consumer<Piece> promotionCallback) {
        // Create a new popup stage
        Stage popupStage = new Stage();
        popupStage.setTitle("Choose Promotion");

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);

        Label label = new Label("Choose your promotion:");
        vbox.getChildren().add(label);

        // Create buttons for each promotion option
        Button queenButton = new Button("Queen");
        Button knightButton = new Button("Knight");
        Button rookButton = new Button("Rook");
        Button bishopButton = new Button("Bishop");

        // Set button actions
        queenButton.setOnAction(e -> {
            promotionCallback.accept(Piece.make(chesslibBoard.getSideToMove(), PieceType.QUEEN));
            popupStage.close();
        });
        knightButton.setOnAction(e -> {
            promotionCallback.accept(Piece.make(chesslibBoard.getSideToMove(), PieceType.KNIGHT));
            popupStage.close();
        });
        rookButton.setOnAction(e -> {
            promotionCallback.accept(Piece.make(chesslibBoard.getSideToMove(), PieceType.ROOK));
            popupStage.close();
        });
        bishopButton.setOnAction(e -> {
            promotionCallback.accept(Piece.make(chesslibBoard.getSideToMove(), PieceType.BISHOP));
            popupStage.close();
        });

        vbox.getChildren().addAll(queenButton, knightButton, rookButton, bishopButton);

        Scene scene = new Scene(vbox, 200, 200);
        popupStage.setScene(scene);
        popupStage.initModality(Modality.APPLICATION_MODAL); // Block user interaction with other windows
        popupStage.showAndWait(); // Wait for the user to choose
    }

    private String getPieceSymbolForPromotion(Piece promotionPiece) {
        String color = promotionPiece.getPieceSide() == Side.WHITE ? "W" : "B";
        String pieceType = switch (promotionPiece.getPieceType()) {
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            default -> "";
        };
        return color + pieceType;
    }


    @FXML
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
        }
    }

    @FXML
    private void goToHomeScreen()
    {
        try {
            // Load the FXML file for the home screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/home_screen.fxml"));
            Parent homeScreen = loader.load();

            // Get the current scene and set the new root
            Stage stage = (Stage) gridPane.getScene().getWindow(); // Assuming the gridPane is already part of the scene
            stage.setScene(new Scene(homeScreen));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    private void showEndGameScreen(String message) {
        // Create a dimming effect on the chessboard
        ColorAdjust dimEffect = new ColorAdjust();
        dimEffect.setBrightness(-0.5);  // Darken the chessboard
        gridPane.setEffect(dimEffect);

        // Set the end-game message
        endGameLabel.setText(message);

        // Make the overlay visible with a fade-in transition
        endGameOverlay.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), endGameOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    // Method to restart the game (optional, if needed)
    @FXML
    private void restartGame() {
        chesslibBoard = new com.github.bhlangonijr.chesslib.Board(); // Reset the chessboard
        initializeGame(isPlayerBlack ? "black" : "white", 1); // Reinitialize the game

        // Hide the end-game overlay
        endGameOverlay.setVisible(false);
        // Clear the dimming effect on the chessboard
        gridPane.setEffect(null);
    }
}