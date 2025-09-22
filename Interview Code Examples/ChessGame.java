class Player {
    private String name;
    private boolean isWhiteSide;

    public Player(String name, boolean isWhiteSide) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isWhiteSide() {
        return isWhiteSide;
    }
}

enum Status {
    ACTIVE, SAVED, BLACK_WIN, WHITE_WIN, STALEMATE;
}

abstract class Piece {
    private boolean isWhitePiece;
    private boolean killed = false;
    private MoveMentStrategy moveMentStrategy;

    public Piece(boolean isWhitePiece, MoveMentStrategy moveMentStrategy) {
        this.isWhitePiece = isWhitePiece;
        this.moveMentStrategy = moveMentStrategy;
    }

    public boolean isWhite() {
        return isWhitePiece;
    }

    public boolean isKilled() {
        return killed;
    }

    public void setKilled(boolean killed) {
        this.killed = killed;
    }

    public boolean canMove(Board board, Cell startBlock, Cell endBlock) {
        return moveMentStrategy.canMove(board, startBlock, endBlock);
    }
}

class King extends Piece {
    private MoveMentStrategy strategy;

    public King(boolean isWhitePiece) {
        super(isWhitePiece, new KingMovementStrategy());
    }

    @Override
    public boolean canMove(Board board, Cell startCell, Cell endCell) {
        return strategy.canMove(board, startCell, endCell);
    }
}

class Queen extends Piece {
    public Queen(boolean isWhitePiece) {
        super(isWhitePiece);
    }
}

class Bishop extends Piece {
    public Bishop(boolean isWhitePiece) {
        super(isWhitePiece);
    }
}

class Knight extends Piece {
    public Knight(boolean isWhitePiece) {
        super(isWhitePiece);
    }
}

class Rook extends Piece {
    public Rook(boolean isWhitePiece) {
        super(isWhitePiece);
    }
}

class Pawn extends Piece {
    public Pawn(boolean isWhitePiece) {
        super(isWhitePiece);
    }
}

abstract class PieceFactory {
    public static Piece createPiece(String pieceType, boolean isWhitePiece) {
        switch (pieceType.toLowerCase()) {
            case "king":
                return new King(isWhitePiece);
            case "queen":
                return new Queen(isWhitePiece);
            case "bishop":
                return new Bishop(isWhitePiece);
            case "knight":
                return new Knight(isWhitePiece);
            case "rook":
                return new Rook(isWhitePiece);
            case "pawn":
                return new Pawn(isWhitePiece);
            default:
                throw new IllegalArgumentException(pieceType);
        }
    }
}

class Cell {
    private int row, col;
    private String label;
    private Piece piece;

    public Cell(int row, int col, Piece piece) {
        this.row = row;
        this.col = col;
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }
}

class Move {
    private Cell startCell;
    private Cell endCell;

    public Move(Cell startCell, Cell endCell) {
        this.startCell = startCell;
        this.endCell = endCell;
    }

    public boolean isValid() {
        return !(startCell.getPiece().isWhite() == endCell.getPiece().isWhite());
    }

    public Cell getStartCell() {
        return startCell;
    }

    public Cell getEndCell() {
        return endCell;
    }
}

class Board {
    private static Board instance;
    private Cell[][] board;

    private Board(int rows) {
        initializeBoard(rows);
    }

    public static Board getInstance(int rows) {
        if (instance == null) {
            instance = new Board(rows);
        }
        return instance;
    }

    private void initializeBoard(int rows) {
        board = new Cell[rows][rows];

        setPieceRow(0, true);
        setPawnRow(1, rows, true);

        setPieceRow(rows - 1, false);
        setPawnRow(rows - 1, rows, false);

        for (int row = 2; row < rows - 2; row++) {
            for (int col = 0; col < rows; col++) {
                board[row][col] = new Cell(row, col, null);
            }
        }
    }

    private void setPieceRow(int row, boolean isWhite) {
        board[row][0] = new Cell(row, 0, PieceFactory.createPiece("rook", isWhite));
        board[row][1] = new Cell(row, 1, PieceFactory.createPiece("knight", isWhite));
        board[row][2] = new Cell(row, 2, PieceFactory.createPiece("bishop", isWhite));
        board[row][3] = new Cell(row, 3, PieceFactory.createPiece("queen", isWhite));
        board[row][4] = new Cell(row, 4, PieceFactory.createPiece("king", isWhite));
        board[row][5] = new Cell(row, 5, PieceFactory.createPiece("rook", isWhite));
        board[row][6] = new Cell(row, 6, PieceFactory.createPiece("bishop", isWhite));
        board[row][7] = new Cell(row, 7, PieceFactory.createPiece("rook", isWhite));
    }

    private void setPawnRow(int row, int rows, boolean isWhite) {
        for (int j = 0; j < rows; j++) {
            board[row][j] = new Cell(row, j, PieceFactory.createPiece("pawn", isWhite));
        }
    }
}

interface MoveMentStrategy {
    boolean canMove(Board board, Cell startCell, Cell endCell);
}

public class ChessGame {

}
