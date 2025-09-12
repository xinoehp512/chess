package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    ChessGame.TeamColor color;
    PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        return switch (type) {
            case KING -> getKingMoves(board, myPosition);
            case QUEEN -> getQueenMoves(board, myPosition);
            case BISHOP -> getBishopMoves(board, myPosition);
            case KNIGHT -> getKnightMoves(board, myPosition);
            case ROOK -> getRookMoves(board, myPosition);
            case PAWN -> getPawnMoves(board, myPosition);
        };
    }

    private ArrayList<ChessPosition> getMoveWidgetPositions(ChessBoard board, ChessPosition myPosition, int[][] moveWidgets, int moveDistance, boolean allowMoves, boolean allowCaptures) {
        ArrayList<ChessPosition> positions = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int[] widget : moveWidgets) {
            int rowMove = widget[0];
            int colMove = widget[1];
            for (int dist = 1; dist <= moveDistance; dist++) {
                int newRow = row + rowMove * dist;
                int newCol = col + colMove * dist;
                var targetPos = new ChessPosition(newRow, newCol);
                if (!board.positionValid(targetPos)) {
                    continue;
                }
                var targetPiece = board.getPiece(targetPos);
                if (targetPiece == null) {
                    if (allowMoves) {
                        positions.add(targetPos);
                    }
                } else {
                    if (targetPiece.color != this.color && allowCaptures) {
                        positions.add(targetPos);
                    }
                    break;
                }
            }

        }
        return positions;
    }

    private HashSet<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {

        int yDirection = 0;
        int doubleStepRow = 0;
        int promotionRow = 0;


        switch (this.color) {
            case WHITE -> {
                yDirection = 1;
                doubleStepRow = 2;
                promotionRow = 7;
            }
            case BLACK -> {
                yDirection = -1;
                doubleStepRow = 7;
                promotionRow = 2;
            }
        }

        int row = myPosition.getRow();
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] moveWidgets = {{yDirection, 0}};
        int[][] moveWidgets2 = {{yDirection, 1}, {yDirection, -1}};
        int moveDistance = row == doubleStepRow ? 2 : 1;

        var positions = getMoveWidgetPositions(board, myPosition, moveWidgets, moveDistance, true, false);
        positions.addAll(getMoveWidgetPositions(board, myPosition, moveWidgets2, moveDistance, false, true));

        PieceType[] promotionPieces = {PieceType.BISHOP, PieceType.ROOK, PieceType.KNIGHT, PieceType.QUEEN};
        for (var targetPos : positions) {
            if (row==promotionRow){
                for (var piece : promotionPieces) {
                    moves.add(new ChessMove(myPosition, targetPos, piece));
                }
            } else {
                moves.add(new ChessMove(myPosition, targetPos, null));
            }
        }




        return moves;
    }

    private HashSet<ChessMove> getKnightMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] moveWidgets = {
                {1, 2}, {2, 1},
                {1, -2}, {2, -1},
                {-1, 2}, {-2, 1},
                {-1, -2}, {-2, -1},
        };
        var positions = getMoveWidgetPositions(board, myPosition, moveWidgets, 1, true, true);

        for (var targetPos : positions) {
            moves.add(new ChessMove(myPosition, targetPos, null));
        }

        return moves;
    }

    private HashSet<ChessMove> getQueenMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = getBishopMoves(board, myPosition);
        moves.addAll(getRookMoves(board, myPosition));
        return moves;
    }

    private HashSet<ChessMove> getRookMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] moveWidgets = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        var positions = getMoveWidgetPositions(board, myPosition, moveWidgets, 7, true, true);

        for (var targetPos : positions) {
            moves.add(new ChessMove(myPosition, targetPos, null));
        }
        return moves;
    }

    private HashSet<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] moveWidgets = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        var positions = getMoveWidgetPositions(board, myPosition, moveWidgets, 7, true, true);

        for (var targetPos : positions) {
            moves.add(new ChessMove(myPosition, targetPos, null));
        }
        return moves;
    }

    private HashSet<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] moveWidgets = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        var positions = getMoveWidgetPositions(board, myPosition, moveWidgets, 1, true, true);

        for (var targetPos : positions) {
            moves.add(new ChessMove(myPosition, targetPos, null));
        }
        return moves;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "color=" + color +
                ", type=" + type +
                '}';
    }
}
