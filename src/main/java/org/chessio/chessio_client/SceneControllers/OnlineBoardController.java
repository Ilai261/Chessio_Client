package org.chessio.chessio_client.SceneControllers;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import jakarta.websocket.*;
import org.chessio.chessio_client.Models.GraphicsBoard;

import java.util.UUID;

@ClientEndpoint
public class OnlineBoardController extends BaseBoardController {

    private Session session;
    private String currentEnemyMoveMessage;
    private UUID gameId;

    @Override
    public void initializeGame(String gameStartMessage)
    {
        String[] parts = gameStartMessage.split("\\|");

        gameId = UUID.fromString(parts[0]);
        isPlayerBlack = parts[2].equals("black");
        playerGraphicsBoard = new GraphicsBoard(isPlayerBlack ? "black" : "white");
        enemyGraphicsBoard = new GraphicsBoard(isPlayerBlack ? "white" : "black");

        // Set the turn logic: White always starts, so check if player is white or black
        isPlayerTurn = !isPlayerBlack;
        if(!isPlayerTurn) {turnLabel.setText("Enemy turn");}

        // Set up the board and begin the game
        createChessBoard();
    }


    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to the server.");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("game message received: " + message);
        Platform.runLater(() -> handleServerMessage(message));
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed: " + closeReason.getReasonPhrase());
    }

    private void handleServerMessage(String message) {

        if (false)
        {

        }
        else
        {
            // Handle enemy moves
            setCurrentEnemyMoveMessage(message);
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
    protected void handleResignAction(ActionEvent actionEvent) {

    }

    private String getMoveUCI(int fromRow, int fromCol, int toRow, int toCol) {
        // Convert to UCI format (e.g., "e2e4")
        String from = getColLetter(fromCol) + (8 - fromRow);
        String to = getColLetter(toCol) + (8 - toRow);
        return from + to;
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
}

