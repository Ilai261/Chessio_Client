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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public abstract class BaseBoardController
{
    @FXML
    protected GridPane gridPane; // Reference to the GridPane from the FXML

    @FXML
    protected Label turnLabel; // Label for "Your turn" (optional)

    @FXML
    protected Label usernameLabel; // For the "Username" label

    @FXML
    protected Label enemyLabel;

    @FXML
    protected VBox endGameOverlay;

    @FXML
    protected Label endGameLabel;


    protected static final int TILE_SIZE = 80; // Size of each tile
    protected static final int BOARD_SIZE = 8; // Chessboard is 8x8

    protected GraphicsBoard playerGraphicsBoard;
    protected GraphicsBoard enemyGraphicsBoard;
    protected boolean isPlayerBlack;
    protected boolean pieceSelected = false;
    protected int selectedRow = -1, selectedCol = -1;
    protected boolean isPlayerTurn;  // Track if it's the player's turn

    protected Board chesslibBoard = new Board();
    protected int enemyLevel;
    protected boolean gameEnded = false;
    protected String gameResult;
    protected String username;
    protected String playerPromotionValue = null;

    // Initialize the game and start Stockfish process
    public abstract void initializeGame(String playerColor);

    protected void createChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Flip the row if the player is black
                int displayRow = getSquareAdjustedRow(row);

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
                    assert piece != null;
                    piece.setOnMouseClicked(e -> handleTileOrPieceClick(displayRow, finalCol, finalPieceSymbol));
                }

                int finalCol1 = col;
                String finalPieceSymbol1 = pieceSymbol;
                tile.setOnMouseClicked(e -> handleTileOrPieceClick(displayRow, finalCol1, finalPieceSymbol1));
            }
        }
    }


    // Method to create a chessboard tile
    protected Rectangle createTile(int row, int col) {
        Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
        tile.setFill((row + col) % 2 == 0 ? Color.rgb(245, 245, 245) : Color.rgb(118, 150, 86));
        tile.setOnMouseClicked(e -> onTileClicked(row, col));
        return tile;
    }

    // Handle both piece and tile clicks in a unified way
    protected void handleTileOrPieceClick(int row, int col, String pieceSymbol)
    {
        if(gameEnded) { return; } // if game has ended ignore the attempt

        if (pieceSelected) {
            // If a piece is already selected, we are attempting to move it
            onTileClicked(row, col);
        } else {
            // If no piece is selected, we are attempting to select a piece
            if (pieceSymbol != null) {
                onPieceClicked(row, col);
            }
        }
    }

    // Handle tile click event for moving pieces
    protected abstract void onTileClicked(int row, int col);

    // Handle piece click event
    protected void onPieceClicked(int row, int col) {
        // Ensure it's the player's turn before allowing them to move
        if (!isPlayerTurn) {
            System.out.println("It's not your turn!");
            return;
        }
        // Select the new piece and highlight its possible moves
        selectedRow = row;
        selectedCol = col;
        highlightTile(row, col); // Highlight the selected tile
        highlightLegalMoves(row, col); // Highlight possible moves
        pieceSelected = true;
    }

    protected void highlightTile(int row, int col) {
        // Adjust row for black's perspective
        int squareAdjustedRow = getSquareAdjustedRow(row);

        // Ensure squareAdjustedRow and col are within valid bounds
        if (squareAdjustedRow >= 0 && squareAdjustedRow < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            for (javafx.scene.Node node : gridPane.getChildren()) {
                if (GridPane.getRowIndex(node) == squareAdjustedRow && GridPane.getColumnIndex(node) == col) {
                    if (node instanceof Rectangle tile) {
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

    protected void highlightLegalMoves(int row, int col) {
        // Adjust row for black's perspective
        int squareAdjustedRow = getSquareAdjustedRow(row);
        Square square = getSquare(squareAdjustedRow, col);

        try {
            // Generate all legal moves from the current board state
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(chesslibBoard);

            // Filter moves that are specific to the given square
            for (Move move : legalMoves) {
                if (move.getFrom().equals(square)) {
                    int toRow = getRowFromUci(move.getTo().value());
                    int toCol = getColFromUci(move.getTo().value());
                    highlightTile(toRow, toCol); // Highlight legal moves
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if a move is legal
    protected boolean isLegalMove(int fromRow, int fromCol, int toRow, int toCol) {
        int squareAdjustedFromRow = getSquareAdjustedRow(fromRow);
        int squareAdjustedToRow = getSquareAdjustedRow(toRow);

        Square from = getSquare(squareAdjustedFromRow, fromCol);
        Square to = getSquare(squareAdjustedToRow, toCol);
        try {
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(chesslibBoard);
            return legalMoves.stream().anyMatch(move -> move.getFrom().equals(from) && move.getTo().equals(to));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected void makePlayerMove(int fromRow, int fromCol, int toRow, int toCol)
    {
        // Adjust the row and column based on the player's color for square
        int squareAdjustedFromRow = getSquareAdjustedRow(fromRow);
        int squareAdjustedToRow = getSquareAdjustedRow(toRow);

        System.out.println("Moving piece from " + fromRow + "," + fromCol + " to " + toRow + "," + toCol);

        Square from = getSquare(squareAdjustedFromRow, fromCol);
        Square to = getSquare(squareAdjustedToRow, toCol);

        // Move the piece in the chesslibBoard
        AtomicReference<Move> move = new AtomicReference<>(new Move(from, to));
        // Check if the move is castling
        Piece movingPiece = chesslibBoard.getPiece(from);

        // Check if the destination tile contains an enemy piece
        AtomicReference<String> pieceSymbol = new AtomicReference<>(playerGraphicsBoard.getPieceAt(fromRow, fromCol));
        String targetPieceSymbol = enemyGraphicsBoard.getPieceAt(toRow, toCol);

        // castling
        if (movingPiece.getPieceType() == PieceType.KING && Math.abs(fromCol - toCol) == 2) {
            makeCastling(fromRow, toCol, playerGraphicsBoard);
        }
        // Handle en passant
        else if (movingPiece.getPieceType() == PieceType.PAWN &&
                !move.get().getFrom().getFile().equals(move.get().getTo().getFile())
                && enemyGraphicsBoard.getPieceAt(toRow, toCol) == null)
        {
            makeEnPassant(toRow, toCol, enemyGraphicsBoard, false);
        }
        // promotion
        else if (movingPiece.getPieceType() == PieceType.PAWN && (toRow == 0 || toRow == BOARD_SIZE - 1)) {
            // Trigger pawn promotion popup
            showPromotionPopup((selectedPromotionPiece) -> {
                // After the player selects a promotion piece, proceed with promotion
                Move promotionMove = new Move(from, to, selectedPromotionPiece);
                pieceSymbol.set(makePawnPromotion(selectedPromotionPiece));
                move.set(promotionMove);
            });
        }
        // make the move needed
        movePlayerPiece(fromRow, fromCol, toRow, toCol, move.get(), pieceSymbol.get(), targetPieceSymbol);
    }

    protected void movePlayerPiece(int fromRow, int fromCol, int toRow, int toCol, Move move,
                                 String pieceSymbol, String targetPieceSymbol)
    {
        // now make the move itself
        if (chesslibBoard.isMoveLegal(move, true))
        {
            chesslibBoard.doMove(move);

            // Player capturing an enemy piece
            if (targetPieceSymbol != null) {
                enemyGraphicsBoard.removePieceAt(toRow, toCol);
            }
            playerGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
            playerGraphicsBoard.removePieceAt(fromRow, fromCol);

            gridPane.getChildren().clear(); // Clear the grid
            createChessBoard(); // Recreate the board with updated pieces

            // check if a mate or a draw has occurred, if yes then go to end screen
            checkForDrawOrMateAndGotoEndScreen();
        }
        else
        {
            System.out.println("Illegal move... " + move);
        }
    }

    protected abstract void fetchEnemyMove();

    protected void moveEnemyPiece(int fromRow, int fromCol, int toRow, int toCol, Move move) {
        // Determine if the move is for the player or the enemy
        String pieceSymbol = enemyGraphicsBoard.getPieceAt(fromRow, fromCol);
        Piece movingPiece = chesslibBoard.getPiece(move.getFrom());

        // Handle castling
        if (movingPiece.getPieceType() == PieceType.KING && Math.abs(fromCol - toCol) == 2) {
            makeCastling(fromRow, toCol, enemyGraphicsBoard);
        }
        // Handle en passant
        else if (movingPiece.getPieceType() == PieceType.PAWN && !move.getFrom().getFile().equals(move.getTo().getFile())
                && playerGraphicsBoard.getPieceAt(toRow, toCol) == null)
        {
            makeEnPassant(toRow, toCol, playerGraphicsBoard, true);
        }
        else if (movingPiece.getPieceType() == PieceType.PAWN && (toRow == 0 || toRow == BOARD_SIZE - 1))
        {
            Piece promotionPiece = move.getPromotion(); // Get the promotion piece
            pieceSymbol = getPieceSymbolForPromotion(promotionPiece); // set the promotion piece
        }

        // Enemy capturing a player piece
        if (playerGraphicsBoard.getPieceAt(toRow, toCol) != null)
        {
            playerGraphicsBoard.removePieceAt(toRow, toCol);
        }

        // Normal move: move the piece to the new position
        enemyGraphicsBoard.setPieceAt(toRow, toCol, pieceSymbol);
        enemyGraphicsBoard.removePieceAt(fromRow, fromCol);

        // Update the grid after the move
        gridPane.getChildren().clear();
        createChessBoard();
    }

    protected int getSquareAdjustedRow(int fromRow) {
        return isPlayerBlack ? BOARD_SIZE - 1 - fromRow : fromRow;
    }

    protected void makeCastling(int fromRow, int toCol, GraphicsBoard graphicsBoard) {
        if (toCol == 6) { // Kingside castling
            graphicsBoard.setPieceAt(fromRow, 5, graphicsBoard.getPieceAt(fromRow, 7)); // Move rook
            graphicsBoard.removePieceAt(fromRow, 7);
        } else if (toCol == 2) { // Queenside castling
            graphicsBoard.setPieceAt(fromRow, 3, graphicsBoard.getPieceAt(fromRow, 0)); // Move rook
            graphicsBoard.removePieceAt(fromRow, 0);
        }
    }

    protected void makeEnPassant(int toRow, int toCol, GraphicsBoard graphicsBoard, boolean isEnemy)
    {
        int captureRow = isPlayerBlack ? toRow - 1 : toRow + 1;
        // if it's the enemy's en passant calculation is different
        if (isEnemy)
        {
            captureRow = isPlayerBlack ? toRow + 1 : toRow - 1;
        }
        graphicsBoard.removePieceAt(captureRow, toCol); // Remove captured pawn
    }

    protected String makePawnPromotion(Piece promotionPiece)
    {
        String symbol = getPieceSymbolForPromotion(promotionPiece);
        this.playerPromotionValue = symbol.substring(1,2).toLowerCase();
        return symbol;
    }

    // Function to return the correct Piece object for promotion
    protected static Piece getPromotionPiece(char promotionChar, Side side) {
        PieceType promotionPieceType = switch (promotionChar) {
            case 'q' ->  // Promotion to Queen
                    PieceType.QUEEN;
            case 'r' ->  // Promotion to Rook
                    PieceType.ROOK;
            case 'b' ->  // Promotion to Bishop
                    PieceType.BISHOP;
            case 'n' ->  // Promotion to Knight
                    PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece: " + promotionChar);
        };

        // Return the Piece object for the side (either Side.WHITE or Side.BLACK)
        return Piece.make(side, promotionPieceType);
    }

    protected void checkForDrawOrMateAndGotoEndScreen()
    {
        // Check for checkmate or stalemate or draw
        if (chesslibBoard.isMated()) {
            System.out.println("Checkmate! Game over.");
            gameEnded = true;
            Side currentSide = chesslibBoard.getSideToMove(); // Whose turn it is in the game

            if (isPlayerBlack && currentSide == Side.BLACK || !isPlayerBlack && currentSide == Side.WHITE) {
                // If it's the player's turn and checkmate, the player lost
                System.out.println("Checkmate! You lost.");
                showEndGameScreen("Checkmate! You lost...");
            } else {
                // If it's the bot's turn and checkmate, the bot lost
                System.out.println("Checkmate! You win!");
                showEndGameScreen("Checkmate! You win!");
            }

            if(currentSide == Side.BLACK) {
                gameResult = "white_win";
            }
            else
            {
                gameResult = "black_win";
            }
        }
        else {
            gameResult = "draw";
            if (chesslibBoard.isStaleMate()) {
                System.out.println("Stalemate! Game over.");
                gameEnded = true;
                showEndGameScreen("Stalemate! It's a draw.");
            } else if (chesslibBoard.isRepetition()) {
                System.out.println("Repetition! Game over.");
                gameEnded = true;
                showEndGameScreen("Repetition! It's a draw.");
            } else if (chesslibBoard.isInsufficientMaterial()) {
                System.out.println("Insufficient material! Game over.");
                gameEnded = true;
                showEndGameScreen("Insufficient material! It's a draw.");
            } else if (chesslibBoard.getHalfMoveCounter() >= 100) {
                System.out.println("50 moves with no captures and no pawn moves! Game over.");
                gameEnded = true;
                showEndGameScreen("Game over! it's a draw (50 moves rule).");
            }
        }
    }

    // Method to clear all highlighted tiles
    protected void clearHighlights() {
        gridPane.getChildren().clear(); // Clear the grid
        createChessBoard(); // Recreate the board without any highlights
    }

    protected Square getSquare(int row, int col) {
        // Flip the row when the player is black, so coordinates are correctly adjusted
        int adjustedRow = getSquareAdjustedRow(row);
        String squareName = (getColLetter(col) + (BOARD_SIZE - adjustedRow)).toUpperCase(); //may need to change here cancel board size

        System.out.println("Square name: " + squareName + " row: " + row + " col: " + col);

        return Square.valueOf(squareName); // Ensure the square is in uppercase
    }

    protected String getColLetter(int col) {
        return String.valueOf((char) ('a' + col));
    }

    protected int getRowFromUci(String uci) {
        return BOARD_SIZE - Character.getNumericValue(uci.charAt(1)); // Correct row conversion
    }

    protected int getColFromUci(String uci) {
        return uci.charAt(0) - 'A'; // 'a' to 'h' should map to 0 to 7
    }

    // Method to create an ImageView for a piece based on the piece symbol

    protected ImageView createPiece(String pieceSymbol) {
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

    protected void showPromotionPopup(Consumer<Piece> promotionCallback) {
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

    protected String getPieceSymbolForPromotion(Piece promotionPiece) {
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
    protected abstract void handleResignAction(ActionEvent actionEvent);

    protected void setUsernameLabel(String username) {
        usernameLabel.setText(username);
    }

    protected void showEndGameScreen(String message) {
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

    @FXML
    protected void goToHomeScreen()
    {
        try {
            // Load the FXML file for the home screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chessio/chessio_client/homeScreen.fxml"));
            Parent homeScreen = loader.load();

            // Get the current scene and set the new root
            Stage stage = (Stage) gridPane.getScene().getWindow(); // Assuming the gridPane is already part of the scene
            stage.setScene(new Scene(homeScreen));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to restart the game
    @FXML
    protected abstract void restartGame();

    public void setEnemyLevel(int level)
    {
        this.enemyLevel = level;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
