package org.chessio.chessio_client.Controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.ColorAdjust;

public class ChessBoardController {

    @FXML
    private GridPane gridPane; // Reference to the GridPane from the FXML

    private static final int TILE_SIZE = 80; // Size of each tile
    private static final int BOARD_SIZE = 8; // Chessboard is 8x8

    private BoardController playerBoardController;
    private BoardController enemyBoardController;
    private boolean isPlayerBlack;

    // Method to initialize the chessboard with the selected color and enemy level
    public void initializeGame(String playerColor, int enemyLevel) {
        // Determine if the player is black
        isPlayerBlack = playerColor.equals("black");

        // Initialize the board for the player and the enemy
        playerBoardController = new BoardController(playerColor);
        enemyBoardController = new BoardController(isPlayerBlack ? "white" : "black");

        // Now, you can use enemyLevel to configure the difficulty or behavior of the enemy later
        System.out.println("Initializing game with player color: " + playerColor + " and enemy level: " + enemyLevel);

        // Create the chessboard display
        createChessBoard();
        printBoard();
    }

    // Method to create an 8x8 chessboard (only graphics)
    private void createChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Create a rectangle for each tile (for visual display)
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setFill((row + col) % 2 == 0 ? Color.rgb(245, 245, 245) : Color.rgb(118, 150, 86));
                gridPane.add(tile, col, row);

                // Adjust the row index depending on whether the player is black or white
                int displayRow = isPlayerBlack ? BOARD_SIZE - 1 - row : row;

                // Add pieces based on board state
                String pieceSymbol = playerBoardController.getPieceAt(displayRow, col);
                if (pieceSymbol == null) {
                    pieceSymbol = enemyBoardController.getPieceAt(displayRow, col);
                }

                // Create an ImageView for the chess piece if there's a piece on the square
                if (pieceSymbol != null) {
                    ImageView piece = createPiece(pieceSymbol);
                    if (piece != null) {
                        gridPane.add(piece, col, row);
                    }
                }
            }
        }
    }

    // Method to create an ImageView for a piece based on the piece symbol
    private ImageView createPiece(String pieceSymbol) {
        // Example of parsing the symbol "WQ" for White Queen or "BP" for Black Pawn
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

                if (color.equals("white")) {
                    // Apply transparency and make white pieces appear brighter
                    imageView.setOpacity(0.99);  // Slightly adjust opacity

                    // Apply brightness effect using ColorAdjust
                    ColorAdjust colorAdjust = new ColorAdjust();
                    colorAdjust.setBrightness(0.1); // Increase brightness
                    imageView.setEffect(colorAdjust);
                }

                return imageView;
            } else {
                System.err.println("Error loading image: " + imagePath);
            }
        }
        return null;
    }

    // Method to print the board content (for debugging)
    private void printBoard() {
        System.out.println("Chessboard Layout:");

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // First check player's board
                String piece = playerBoardController.getPieceAt(row, col);
                if (piece == null) {
                    // Then check enemy's board if player's piece is not present
                    piece = enemyBoardController.getPieceAt(row, col);
                }
                System.out.print((piece != null ? piece : "--") + " ");
            }
            System.out.println(); // Move to the next line after each row
        }

        System.out.println(); // Extra line after the board print
    }
}
