package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    ChessPosition startPos;
    ChessPosition endPos;
    ChessPiece.PieceType promoPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        startPos=startPosition;
        endPos=endPosition;
        promoPiece=promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPos;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPos;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promoPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(startPos, chessMove.startPos) && Objects.equals(endPos, chessMove.endPos) && promoPiece == chessMove.promoPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPos, endPos, promoPiece);
    }

    @Override
    public String toString() {
        return "ChessMove{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                ", promoPiece=" + promoPiece +
                '}';
    }
}
