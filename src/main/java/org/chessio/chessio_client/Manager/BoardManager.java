// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class configures the board manager layout

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

    // load a board from a FEN string (starting a new game or continuing a saved game)
    public void loadFen(String fen) {
        board.loadFromFen(fen);
    }

    // get legal moves for a specific piece at the given position
    public List<String> getLegalMoves(Square square) {
        List<String> legalMovesUci = new ArrayList<>();

        try {
            // generate all legal moves from the current board state
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);

            // filter moves that are specific to the given piece
            for (Move move : legalMoves) {
                if (move.getFrom().equals(square)) {
                    // Add the move in UCI format (e.g., "e2e4")
                    legalMovesUci.add(move.toString());
                }
            }
        } catch (Exception e) {
            System.out.println("error while attempting to generate legal moves: " + e.getMessage());
        }

        return legalMovesUci;
    }

    // check if the current board is in a winning state (checkmate)
    public boolean isWinningPosition() {
        return board.isMated();
    }

    // check if the current board is in a stalemate
    public boolean isStalemate() {
        return board.isStaleMate();
    }

    // print the current board state
    public void printBoard() {
        System.out.println(board.toString());
    }

    // move a piece on the board using UCI notation
    public void movePiece(String uciMove) {
        Move move = new Move(uciMove, board.getSideToMove());
        if (board.isMoveLegal(move, true)) {
            board.doMove(move);
        } else {
            System.out.println("Illegal move: " + uciMove);
        }
    }

    // get the current FEN string of the board
    public String getFen() {
        return board.getFen();
    }

    // reset the board to the initial position
    public void resetBoard() {
        board = new Board();
    }
}