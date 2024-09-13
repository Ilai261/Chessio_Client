package org.chessio.chessio_client.SceneControllers;


public class BoardController {

    private String[][] board;
    private String color;

    public BoardController(String color) {
        this.color = color;
        this.board = new String[8][8];
        initializeBoard();
    }

    // Initialize the board based on the color (white or black)
    private void initializeBoard() {
        if (color.equals("white")) {
            // Initialize white pieces
            board[6] = new String[]{"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"}; // Pawns
            board[7] = new String[]{"WR", "WN", "WB", "WQ", "WK", "WB", "WN", "WR"}; // Rooks, Knights, Bishops, etc.
        } else {
            // Initialize black pieces
            board[1] = new String[]{"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"}; // Pawns
            board[0] = new String[]{"BR", "BN", "BB", "BQ", "BK", "BB", "BN", "BR"}; // Rooks, Knights, Bishops, etc.
        }
    }

    // Get the piece at a given row and column
    public String getPieceAt(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return board[row][col];
        }
        return null;
    }
}
