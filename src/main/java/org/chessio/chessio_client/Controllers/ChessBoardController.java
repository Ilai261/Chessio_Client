package org.chessio.chessio_client.Controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ChessBoardController {

    @FXML
    private GridPane gridPane; // Reference to the GridPane from the FXML

    private static final int TILE_SIZE = 80; // Size of each tile
    private static final int BOARD_SIZE = 8; // Chessboard is 8x8

    @FXML
    public void initialize() {
        // Create the chessboard
        createChessBoard();
    }

    // Method to create an 8x8 chessboard
    private void createChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Create a rectangle for each tile
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setFill((row + col) % 2 == 0 ? Color.rgb(245, 245, 245) : Color.rgb(118, 150, 86));
                gridPane.add(tile, col, row);

                // Add chess pieces on the first two and last two rows
                if (row == 0 || row == 1 || row == 6 || row == 7) {
                    // Create an ImageView for the chess piece
                    ImageView piece = createPiece(row, col);
                    if (piece != null) {
                        gridPane.add(piece, col, row);
                    }
                }
            }
        }
    }

    private ImageView createPiece(int row, int col) {
        String pieceName = null;
        boolean isBlack = (row == 0 || row == 1); // Black pieces on rows 0 and 1, white on 6 and 7
        String color = isBlack ? "black" : "white";

        if (row == 1 || row == 6) {
            pieceName = "pawn";
        } else {
            switch (col) {
                case 0, 7 -> pieceName = "rook";
                case 1, 6 -> pieceName = "knight";
                case 2, 5 -> pieceName = "bishop";
                case 3 -> pieceName = isBlack ? "queen" : "king";
                case 4 -> pieceName = isBlack ? "king" : "queen";
            }
        }

        if (pieceName != null) {
            // Update the path to point to the correct location of the icons
            String imagePath = "/org/chessio/chessio_client/Icons/" + color + "_" + pieceName + ".png";
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                System.err.println("Error loading image: " + imagePath);
            } else {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(TILE_SIZE * 0.8); // Adjust the image size to fit the tile
                imageView.setFitHeight(TILE_SIZE * 0.8);
                return imageView;
            }
        }
        return null;
    }


}
