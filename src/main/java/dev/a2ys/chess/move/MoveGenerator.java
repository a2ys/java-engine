package dev.a2ys.chess.move;

import dev.a2ys.chess.board.BitboardConstants;

public class MoveGenerator {
    public long getKnightAttacks(int square) {
        long knight = 1L << square;
        long attacks = 0L;

        attacks |= ((knight << 17) & BitboardConstants.NOT_FILE_H)  // Up 2, Left 1
                | ((knight << 15) & BitboardConstants.NOT_FILE_A)   // Up 2, Right 1
                | ((knight >> 17) & BitboardConstants.NOT_FILE_H)   // Down 2, Left 1
                | ((knight >> 15) & BitboardConstants.NOT_FILE_A)   // Down 2, Right 1
                | ((knight << 10) & BitboardConstants.NOT_FILE_GH)  // Up 1, Left 2
                | ((knight << 6) &  BitboardConstants.NOT_FILE_AB)  // Up 1, Right 2
                | ((knight >> 10) & BitboardConstants.NOT_FILE_GH)  // Down 1, Left 2
                | ((knight >> 6) & BitboardConstants.NOT_FILE_AB);  // Down 1, Right 2

        return attacks;
    }

    // Right now, this is using ray-casting, I will learn magic bitboards and optimize this.
    public long getDiagonalAttacks(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        // Northeast direction
        for (int r = rank + 1, f = file + 1; r < 8 && f < 8; r++, f++) {
            int targetSquare = r * 8 + f;
            long targetBit = 1L << targetSquare;
            attacks |= targetBit;

            if ((occupied & targetSquare) != 0) break;
        }

        // Northwest direction
        for (int r = rank + 1, f = file - 1; r < 8 && f >= 0; r++, f--) {
            int targetSquare = r * 8 + f;
            long targetBit = 1L << targetSquare;
            attacks |= targetBit;

            if ((occupied & targetBit) != 0) break;
        }

        // Southeast direction
        for (int r = rank - 1, f = file + 1; r >= 0 && f < 8; r--, f++) {
            int targetSquare = r * 8 + f;
            long targetBit = 1L << targetSquare;
            attacks |= targetBit;

            if ((occupied & targetBit) != 0) break;
        }

        // Southwest direction
        for (int r = rank - 1, f = file - 1; r >= 0 && f >= 0; r--, f--) {
            int targetSquare = r * 8 + f;
            long targetBit = 1L << targetSquare;
            attacks |= targetBit;

            if ((occupied & targetBit) != 0) break;
        }

        return attacks;
    }

    // Right now, this is using ray-casting, I will learn magic bitboards and optimize this.
    public long getStraightAttacks(int square, long occupied) {
        long attacks = 0L;

        // North
        for (int s = square + 8; s < 64; s += 8) {
            attacks |= (1L << s);
            if ((occupied & (1L << s)) != 0) break;
        }

        // South
        for (int s = square - 8; s >= 0; s -= 8) {
            attacks |= (1L << s);
            if ((occupied & (1L << s)) != 0) break;
        }

        // West
        for (int s = square + 1; s % 8 != 0 && s < 64; s++) {
            attacks |= (1L << s);
            if ((occupied & (1L << s)) != 0) break;
        }

        // East
        for (int s = square - 1; s % 8 != 7 && s >= 0; s--) {
            attacks |= (1L << s);
            if ((occupied & (1L << s)) != 0) break;
        }

        return attacks;
    }
}
