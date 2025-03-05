package dev.a2ys.chess.move;

import dev.a2ys.chess.board.BitboardConstants;
import dev.a2ys.chess.board.Board;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    public long getKingAttacks(int square) {
        long king = 1L << square;
        long attacks = 0L;

        // Generate king attacks in all 8 directions
        attacks |= ((king << 8));                                   // North
        attacks |= ((king >> 8));                                   // South
        attacks |= ((king << 1) & BitboardConstants.NOT_FILE_A);    // East
        attacks |= ((king >> 1) & BitboardConstants.NOT_FILE_H);    // West
        attacks |= ((king << 9) & BitboardConstants.NOT_FILE_A);    // Northeast
        attacks |= ((king << 7) & BitboardConstants.NOT_FILE_H);    // Northwest
        attacks |= ((king >> 7) & BitboardConstants.NOT_FILE_A);    // Southeast
        attacks |= ((king >> 9) & BitboardConstants.NOT_FILE_H);    // Southwest

        return attacks;
    }

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

    public long getQueenAttacks(int square, long occupied) {
        return getDiagonalAttacks(square, occupied) | getStraightAttacks(square, occupied);
    }

    // Function to generate all the knight moves
    public List<Move> generateKnightMoves(Board board, boolean isWhite) {
        List<Move> moves = new ArrayList<>();

        // These are the knights from our side
        long knights = isWhite ? board.getWhiteKnights() : board.getBlackKnights();

        // These are the pieces from both sides
        long friendlyPieces = isWhite ? board.getWhitePieces() : board.getBlackPieces();
        long enemyPieces = isWhite ? board.getWhitePieces() : board.getBlackPieces();

        // Go through each knight
        while (knights != 0) {
            // Get the position of the knight, i.e. where the first LSB set to 1
            // This will be the currently selected knight
            int fromSquare = Long.numberOfTrailingZeros(knights);

            // Remove this knight from our knight bitboard
            knights &= knights - 1;

            // Get all attack squares for the currently selected knight
            long attacks = getKnightAttacks(fromSquare);

            // Remove squares occupied by friendly pieces
            attacks &= ~friendlyPieces;

            // Go through each possible move to generate move objects
            while (attacks != 0) {
                // Get the position of the target square, i.e. where the first LSB is set to 1
                int toSquare = Long.numberOfTrailingZeros(attacks);

                // Remove this destination from our attacks bitboard
                attacks &= attacks - 1;

                // Now, we determine if this is a capture move or a quiet move
                if ((1L << toSquare & enemyPieces) != 0) {
                    // This is a capture move
                    moves.add(new Move(fromSquare, toSquare, Move.CAPTURE));
                } else {
                    // This is a quiet move
                    moves.add(new Move(fromSquare, toSquare, Move.QUIET_MOVE));
                }
            }
        }

        return moves;
    }

    // I will not be commenting that much from now on, as the logic behind the steps would be exactly the same as the knight one

    // Function to generate all the bishop moves
    public List<Move> generateBishopMoves(Board board, boolean isWhite) {
        List<Move> moves = new ArrayList<>();

        long bishops = isWhite ? board.getWhiteBishops() : board.getBlackBishops();

        long friendlyPieces = isWhite ? board.getWhitePieces() : board.getBlackPieces();
        long enemyPieces = isWhite ? board.getBlackPieces() : board.getWhitePieces();
        long occupied = board.getOccupied();

        while (bishops != 0) {
            int fromSquare = Long.numberOfTrailingZeros(bishops);
            bishops &= bishops - 1;

            long attacks = getDiagonalAttacks(fromSquare, occupied);
            attacks &= ~friendlyPieces;

            while (attacks != 0) {
                int toSquare = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                if ((1L << toSquare & enemyPieces) != 0) {
                    moves.add(new Move(fromSquare, toSquare, Move.CAPTURE));
                } else {
                    moves.add(new Move(fromSquare, toSquare, Move.QUIET_MOVE));
                }
            }
        }

        return moves;
    }

    public List<Move> generateRookMoves(Board board, boolean isWhite) {
        List<Move> moves = new ArrayList<>();

        long rooks = isWhite ? board.getWhiteRooks() : board.getBlackRooks();

        long friendlyPieces = isWhite ? board.getWhitePieces() : board.getBlackPieces();
        long enemyPieces = isWhite ? board.getBlackPieces() : board.getWhitePieces();
        long occupied = board.getOccupied();

        while (rooks != 0) {
            int fromSquare = Long.numberOfTrailingZeros(rooks);
            rooks &= rooks - 1;

            long attacks = getStraightAttacks(fromSquare, occupied);
            attacks &= ~friendlyPieces;

            while (attacks != 0) {
                int toSquare = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                if ((1L << toSquare & enemyPieces) != 0) {
                    moves.add(new Move(fromSquare, toSquare, Move.CAPTURE));
                } else {
                    moves.add(new Move(fromSquare, toSquare, Move.QUIET_MOVE));
                }
            }
        }

        return moves;
    }

    public List<Move> generateQueenMoves(Board board, boolean isWhite) {
        List<Move> moves = new ArrayList<>();

        long queens = isWhite ? board.getWhiteQueens() : board.getBlackQueens();

        long friendlyPieces = isWhite ? board.getWhitePieces() : board.getBlackPieces();
        long enemyPieces = isWhite ? board.getBlackPieces() : board.getWhitePieces();
        long occupied = board.getOccupied();

        while (queens != 0) {
            int fromSquare = Long.numberOfTrailingZeros(queens);
            queens &= queens - 1;

            long attacks = getQueenAttacks(fromSquare, occupied);
            attacks &= ~friendlyPieces;

            while (attacks != 0) {
                int toSquare = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1;

                if ((1L << toSquare & enemyPieces) != 0) {
                    moves.add(new Move(fromSquare, toSquare, Move.CAPTURE));
                } else {
                    moves.add(new Move(fromSquare, toSquare, Move.QUIET_MOVE));
                }
            }
        }

        return moves;
    }

    public List<Move> generateKingMoves(Board board, boolean isWhite) {
        List<Move> moves = new ArrayList<>();

        long king = isWhite ? board.getWhiteKing() : board.getBlackKing();
        int kingSquare = Long.numberOfTrailingZeros(king);

        long friendlyPieces = isWhite ? board.getWhitePieces() : board.getBlackPieces();
        long enemyPieces = isWhite ? board.getBlackPieces() : board.getWhitePieces();

        long attacks = getKingAttacks(kingSquare);

        attacks &= ~friendlyPieces;

        while (attacks != 0) {
            int toSquare = Long.numberOfTrailingZeros(attacks);
            attacks &= attacks - 1;

            if ((1L << toSquare & enemyPieces) != 0) {
                moves.add(new Move(kingSquare, toSquare, Move.CAPTURE));
            } else {
                moves.add(new Move(kingSquare, toSquare, Move.QUIET_MOVE));
            }
        }

        addCastlingMoves(board, isWhite, kingSquare, moves);

        return moves;
    }

    private void addCastlingMoves(Board board, boolean isWhite, int kingSquare, List<Move> moves) {
        if (isWhite) {
            // White kingside castling
            if (board.canWhiteSideCastleKingside()
                    && (board.getOccupied() & 0x0000000000000006L) == 0     // Check if the F1 and G1 squares are empty
                    && !isSquareAttacked(board, 4, false)  // Check if the white king is not attacked
                    && !isSquareAttacked(board, 5, false)  // Check if the F1 square is not attacked
                    && !isSquareAttacked(board, 6, false)  // Check if the G1 square is not attacked
            ) {
                moves.add(new Move(4, 6, Move.KING_CASTLE));
            }

            // White queenside castling
            if (board.canWhiteSideCastleQueenside()
                    && (board.getOccupied() & 0x0000000000000070L) == 0     // Check if the B1, C1 and D1 squares are empty
                    && !isSquareAttacked(board, 4, false)  // Check if the white king is not attacked
                    && !isSquareAttacked(board, 3, false)  // Check if the D1 square is not attacked
                    && !isSquareAttacked(board, 2, false)  // Check if the C1 square is not attacked
            ) {
                moves.add(new Move(4, 2, Move.QUEEN_CASTLE));
            }
        } else {
            // Black kingside castling
            if (board.canBlackSideCastleKingside() &&
                    (board.getOccupied() & 0x6000000000000000L) == 0 &&     // Check if the F8 and G8 squares are empty
                    !isSquareAttacked(board, 60, true) &&  // Check if the black king is not attacked
                    !isSquareAttacked(board, 61, true) &&  // Check if the F8 square is not attacked
                    !isSquareAttacked(board, 62, true)     // Check if the G8 square is not attacked
            ) {
                moves.add(new Move(60, 62, Move.KING_CASTLE));
            }

            // Black queenside castling
            if (board.canBlackSideCastleQueenside() &&
                    (board.getOccupied() & 0x7000000000000000L) == 0 &&     // Check if the B8, C8, and D8 squares are empty
                    !isSquareAttacked(board, 60, true) &&  // Check the black king is not attacked
                    !isSquareAttacked(board, 59, true) &&  // Check if the D8 square is not attacked
                    !isSquareAttacked(board, 58, true)     // Check if the C8 square is not attacked
            ) {
                moves.add(new Move(60, 58, Move.QUEEN_CASTLE));
            }
        }
    }

    public List<Move> generatePawnMoves(Board board, boolean isWhite) {
        List<Move> moves = new ArrayList<>();

        long pawns = isWhite ? board.getWhitePawns() : board.getBlackPawns();

        long allPieces = board.getOccupied();
        long emptySquares = board.getEmpty();
        long enemyPieces = isWhite ? board.getBlackPieces() : board.getWhitePieces();

        // Push direction and start/promotion rank assignment based on color
        int pushDirection = isWhite ? 8 : -8;
        long startRank = isWhite ? BitboardConstants.RANK_2 : BitboardConstants.RANK_7;
        long promotionRank = isWhite ? BitboardConstants.RANK_8 : BitboardConstants.RANK_1;
        long rankBeforePromotion = isWhite ? BitboardConstants.RANK_7 : BitboardConstants.RANK_2;

        // Generate single push
        long singlePush = isWhite
                ? ((pawns << 8) & emptySquares & ~promotionRank)
                : ((pawns >> 8) & emptySquares & ~promotionRank);

        while (singlePush != 0) {
            // Get the landing square
            int toSquare = Long.numberOfTrailingZeros(singlePush);
            singlePush &= singlePush - 1;

            // Get the starting square which is just 8 squares before and after the toSquare depending on the color
            // The variable pushDirection is stored exactly for this scenario
            int fromSquare = toSquare - pushDirection;
            moves.add(new Move(fromSquare, toSquare, Move.QUIET_MOVE));
        }

        // Here we are checking for the pawns which are on the first rank and can double push
        // These pawns are capable of doing a double push
        long doublePushCandidates = isWhite
                ? ((pawns << 8 & startRank) & emptySquares)
                : ((pawns >> 8 & startRank) & emptySquares);

        // So, we are going to give all the pawns a double push who qualify for a double push above
        long doublePush = isWhite
                ? ((doublePushCandidates << 8) & emptySquares)
                : ((doublePushCandidates >> 8) & emptySquares);

        // The same logic as the single push, the only change is the move type
        while (doublePush != 0) {
            int toSquare = Long.numberOfTrailingZeros(doublePush);
            doublePush &= doublePush - 1;

            int fromSquare = toSquare - (pushDirection * 2);
            moves.add(new Move(fromSquare, toSquare, Move.DOUBLE_PAWN_PUSH));
        }

        // Generating captures to the left of the piece
        // Depending on the color, the bit shift direction and number of bit shifts differ
        long leftCaptures;
        if (isWhite) {
            leftCaptures = ((pawns << 9) & BitboardConstants.NOT_FILE_H & enemyPieces & ~promotionRank);
        } else {
            leftCaptures = ((pawns >> 7) & BitboardConstants.NOT_FILE_H & enemyPieces & ~promotionRank);
        }

        // Same logic for the toSquare and the fromSquare as the single push or the double push
        // Only the calculation of the fromSquare changes as it now decreases or increases by a fixed value
        while (leftCaptures != 0) {
            int toSquare = Long.numberOfTrailingZeros(leftCaptures);
            leftCaptures &= leftCaptures - 1;

            int fromSquare = isWhite ? toSquare - 9 : toSquare + 7;
            moves.add(new Move(fromSquare, toSquare, Move.CAPTURE));
        }

        // Now, generating captures to the right of the piece
        long rightCaptures;
        if (isWhite) {
            rightCaptures = ((pawns << 7) & BitboardConstants.NOT_FILE_A & enemyPieces & ~promotionRank);
        } else {
            rightCaptures = ((pawns >> 9) & BitboardConstants.NOT_FILE_A & enemyPieces & ~promotionRank);
        }

        // Same logic as the left captures
        while (rightCaptures != 0) {
            int toSquare = Long.numberOfTrailingZeros(rightCaptures);
            rightCaptures &= rightCaptures - 1;

            int fromSquare = isWhite ? toSquare - 7 : toSquare + 9;
            moves.add(new Move(fromSquare, toSquare, Move.CAPTURE));
        }

        // TODO: Promotions and En-passant captures

        return moves;
    }

    public boolean isSquareAttacked(Board board, int square, boolean byWhite) {
        // Get the target square as a bitboard
        long targetSquare = 1L << square;

        if (byWhite) {
            // Check if white pawns attack the square
            long whitePawns = board.getWhitePawns();
            if ((((targetSquare >> 7) & BitboardConstants.NOT_FILE_A) & whitePawns) != 0 ||
                    (((targetSquare >> 9) & BitboardConstants.NOT_FILE_H) & whitePawns) != 0) {
                return true;
            }

            // Check if white knights attack the square
            long whiteKnights = board.getWhiteKnights();
            if ((getKnightAttacks(square) & whiteKnights) != 0) {
                return true;
            }

            // Check if white king attacks the square
            long whiteKing = board.getWhiteKing();
            if ((getKingAttacks(square) & whiteKing) != 0) {
                return true;
            }

            // Check if white bishops or queens attack on diagonals
            long whiteBishopsAndQueens = board.getWhiteBishops() | board.getWhiteQueens();
            if ((getDiagonalAttacks(square, board.getOccupied()) & whiteBishopsAndQueens) != 0) {
                return true;
            }

            // Check if white rooks or queens attack on files/ranks
            long whiteRooksAndQueens = board.getWhiteRooks() | board.getWhiteQueens();
            if ((getStraightAttacks(square, board.getOccupied()) & whiteRooksAndQueens) != 0) {
                return true;
            }
        } else {
            // Check if black pawns attack the square
            long blackPawns = board.getBlackPawns();
            if ((((targetSquare << 7) & BitboardConstants.NOT_FILE_H) & blackPawns) != 0 ||
                    (((targetSquare << 9) & BitboardConstants.NOT_FILE_A) & blackPawns) != 0) {
                return true;
            }

            // Check if black knights attack the square
            long blackKnights = board.getBlackKnights();
            if ((getKnightAttacks(square) & blackKnights) != 0) {
                return true;
            }

            // Check if black king attacks the square
            long blackKing = board.getBlackKing();
            if ((getKingAttacks(square) & blackKing) != 0) {
                return true;
            }

            // Check if black bishops or queens attack on diagonals
            long blackBishopsAndQueens = board.getBlackBishops() | board.getBlackQueens();
            if ((getDiagonalAttacks(square, board.getOccupied()) & blackBishopsAndQueens) != 0) {
                return true;
            }

            // Check if black rooks or queens attack on files/ranks
            long blackRooksAndQueens = board.getBlackRooks() | board.getBlackQueens();
            if ((getStraightAttacks(square, board.getOccupied()) & blackRooksAndQueens) != 0) {
                return true;
            }
        }

        // No attacks found
        return false;
    }
}
