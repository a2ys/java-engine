package dev.a2ys.chess.board;

public class Board {
    // Bitboards for pieces
    private long whitePawns;
    private long whiteKnights;
    private long whiteBishops;
    private long whiteRooks;
    private long whiteQueens;
    private long whiteKing;

    private long blackPawns;
    private long blackKnights;
    private long blackBishops;
    private long blackRooks;
    private long blackQueens;
    private long blackKing;

    // Game state
    private boolean whiteToMove;
    private boolean whiteKingsideCastle;
    private boolean whiteQueensideCastle;
    private boolean blackKingsideCastle;
    private boolean blackQueensideCastle;
    private int enPassantSquare;

    public Board() {
        whitePawns = BitboardConstants.WHITE_PAWNS_INITIAL;
        whiteKnights = BitboardConstants.WHITE_KNIGHTS_INITIAL;
        whiteBishops = BitboardConstants.WHITE_BISHOPS_INITIAL;
        whiteRooks = BitboardConstants.WHITE_ROOKS_INITIAL;
        whiteQueens = BitboardConstants.WHITE_QUEENS_INITIAL;
        whiteKing = BitboardConstants.WHITE_KINGS_INITIAL;

        blackPawns = BitboardConstants.BLACK_PAWNS_INITIAL;
        blackKnights = BitboardConstants.BLACK_KNIGHTS_INITIAL;
        blackBishops = BitboardConstants.BLACK_BISHOPS_INITIAL;
        blackRooks = BitboardConstants.BLACK_ROOKS_INITIAL;
        blackQueens = BitboardConstants.BLACK_QUEENS_INITIAL;
        blackKing = BitboardConstants.BLACK_KINGS_INITIAL;

        whiteToMove = true;
        whiteKingsideCastle = true;
        whiteQueensideCastle = true;
        blackKingsideCastle = true;
        blackQueensideCastle = true;
        enPassantSquare = -1;
    }

    public long getWhitePieces() {
        return whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
    }

    public long getBlackPieces() {
        return blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
    }

    public long getOccupied() {
        return getWhitePieces() | getBlackPieces();
    }

    public long getEmpty() {
        return ~getOccupied();
    }

    public void printBoard() {
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                long squareBit = 1L << square;
                char piece = getPieceChar(squareBit);
                System.out.print(piece + " ");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
        System.out.println("Side to move: " + (whiteToMove ? "White" : "Black"));
    }

    public char getPieceChar(long squareBit) {
        if ((whitePawns & squareBit) != 0) return 'P';
        if ((whiteKnights & squareBit) != 0) return 'N';
        if ((whiteBishops & squareBit) != 0) return 'B';
        if ((whiteRooks & squareBit) != 0) return 'R';
        if ((whiteQueens & squareBit) != 0) return 'Q';
        if ((whiteKing & squareBit) != 0) return 'K';

        if ((blackPawns & squareBit) != 0) return 'p';
        if ((blackKnights & squareBit) != 0) return 'n';
        if ((blackBishops & squareBit) != 0) return 'b';
        if ((blackRooks & squareBit) != 0) return 'r';
        if ((blackQueens & squareBit) != 0) return 'q';
        if ((blackKing & squareBit) != 0) return 'k';

        return '.';
    }
}
