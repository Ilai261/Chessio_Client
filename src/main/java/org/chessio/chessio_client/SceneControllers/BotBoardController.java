package org.chessio.chessio_client.SceneControllers;

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

    private com.github.bhlangonijr.chesslib.Board chesslibBoard = new com.github.bhlangonijr.chesslib.Board();

    // Initialize the game
    public void initializeGame(String playerColor, int enemyLevel) {
        isPlayerBlack = playerColor.equals("black");
        playerGraphicsBoard = new GraphicsBoard(playerColor);
        enemyGraphicsBoard = new GraphicsBoard(isPlayerBlack ? "white" : "black");

        // Set the turn logic: White always starts, so check if player is white or black
        isPlayerTurn = !isPlayerBlack; // Player's turn if they are white

        createChessBoard();
    }



    // Method to create the chessboard with pieces
    private void createChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
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

                if (pieceSymbol != null) {
                    ImageView piece = createPiece(pieceSymbol);
                    gridPane.add(piece, col, row);
                    // Attach a click event listener for pieces
                    piece.setOnMouseClicked(e -> onPieceClicked(finalRow, finalCol, finalPieceSymbol));
                }
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
            isPlayerTurn = !isPlayerTurn;
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
                    int toCol = getColFromUci(move.getTo().value().toLowerCase());
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

            // Update the piece in the GraphicsBoard (both player and enemy boards)
            String pieceSymbol = playerGraphicsBoard.getPieceAt(fromRow, fromCol);
            if (pieceSymbol == null) {
                pieceSymbol = enemyGraphicsBoard.getPieceAt(fromRow, fromCol);
                enemyGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
                enemyGraphicsBoard.removePieceAt(fromRow, fromCol);
            } else {
                playerGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
                playerGraphicsBoard.removePieceAt(fromRow, fromCol);
            }

            // Reset the board UI
            gridPane.getChildren().clear(); // Clear the existing pieces and tiles
            createChessBoard(); // Re-create the chessboard after the move

            // Switch the turn after a move
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
        return uci.charAt(0) - 'a'; // 'a' to 'h' should map to 0 to 7
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
