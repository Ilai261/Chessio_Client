// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class defines the chess board for white and black, and serves as the code representation of the gui board.
// each player has one GraphicsBoard object

package org.chessio.chessio_client.Models;

public class GraphicsBoard {

    private final String[][] board;
    private final String color;

    public GraphicsBoard(String color) {
        this.color = color;
        this.board = new String[8][8];
        initializeBoard();
    }

    // initialize the board based on the color (white or black)
    private void initializeBoard() {
        if (color.equals("white")) {
            // Initialize white pieces
            board[6] = new String[]{"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"}; // pawns
            board[7] = new String[]{"WR", "WN", "WB", "WQ", "WK", "WB", "WN", "WR"}; // rooks, knights, bishops, etc.
        } else {
            // Initialize black pieces
            board[1] = new String[]{"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"}; // pawns
            board[0] = new String[]{"BR", "BN", "BB", "BQ", "BK", "BB", "BN", "BR"}; // rooks, knights, bishops, etc.
        }
    }

    // get the piece at a given row and column
    public String getPieceAt(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return board[row][col];
        }
        return null;
    }

    public void setPieceAt(int row, int col, String pieceSymbol) {
        board[row][col] = pieceSymbol;
    }

    public void removePieceAt(int row, int col) {
        board[row][col] = null;
    }
}
