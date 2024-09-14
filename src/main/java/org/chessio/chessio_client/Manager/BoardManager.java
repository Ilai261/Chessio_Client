
package org.chessio.chessio_client.Manager;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;

import java.util.ArrayList;
import java.util.List;

public class BoardManager {

    private Board board;

    public BoardManager() {
        this.board = new Board();
    }

    // Load a board from a FEN string (starting a new game or continuing a saved game)
    public void loadFen(String fen) {
        board.loadFromFen(fen);
    }

    // Get legal moves for a specific piece at the given position
    public List<String> getLegalMoves(Square square) {
        List<String> legalMovesUci = new ArrayList<>();

        try {
            // Generate all legal moves from the current board state
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);

            // Filter moves that are specific to the given piece
            for (Move move : legalMoves) {
                if (move.getFrom().equals(square)) {
                    // Add the move in UCI format (e.g., "e2e4")
                    legalMovesUci.add(move.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return legalMovesUci;
    }

    // Check if the current board is in a winning state (checkmate)
    public boolean isWinningPosition() {
        return board.isMated();
    }

    // Check if the current board is in a stalemate
    public boolean isStalemate() {
        return board.isStaleMate();
    }

    // Print the current board state
    public void printBoard() {
        System.out.println(board.toString());
    }

    // Move a piece on the board using UCI notation
    public void movePiece(String uciMove) {
        Move move = new Move(uciMove, board.getSideToMove());
        if (board.isMoveLegal(move, true)) {
            board.doMove(move);
        } else {
            System.out.println("Illegal move: " + uciMove);
        }
    }

    // Get the current FEN string of the board
    public String getFen() {
        return board.getFen();
    }

    // Reset the board to the initial position
    public void resetBoard() {
        board = new Board();
    }
}