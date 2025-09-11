package chess;

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
        HashSet<ChessMove> moves = switch (type) {
            case KING -> getKingMoves(board, myPosition);
            case QUEEN -> getKingMoves(board, myPosition);
            case BISHOP -> getBishopMoves(board, myPosition);
            case KNIGHT -> getKingMoves(board, myPosition);
            case ROOK -> getRookMoves(board, myPosition);
            case PAWN -> getKingMoves(board, myPosition);
        };

        return moves;
    }

    private HashSet<ChessMove> getRookMoves(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        HashSet<ChessMove> moves = new HashSet<ChessMove>();
        int[][] moveWidgets = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] widget : moveWidgets) {
            int row_move=widget[0];
            int col_move=widget[1];
            for (int dist = 1; dist < 8; dist++) {
                int new_row = row + row_move * dist;
                int new_col = col + col_move * dist;
                var target_pos = new ChessPosition(new_row, new_col);
                if (!board.positionValid(target_pos)) {
                    continue;
                }
                var target_piece = board.getPiece(target_pos);
                if (target_piece == null) {
                    moves.add(new ChessMove(myPosition, target_pos, null));
                } else {
                    if (target_piece.color != this.color) {
                        moves.add(new ChessMove(myPosition, target_pos, null));
                    }
                    break;
                }
            }

        }
        return moves;
    }

    private HashSet<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        HashSet<ChessMove> moves = new HashSet<ChessMove>();
        for (int row_move = -1; row_move <= 1; row_move += 2) {
            for (int col_move = -1; col_move <= 1; col_move += 2) {
                for (int dist = 1; dist < 8; dist++) {
                    int new_row = row + row_move * dist;
                    int new_col = col + col_move * dist;
                    var target_pos = new ChessPosition(new_row, new_col);
                    if (!board.positionValid(target_pos)) {
                        continue;
                    }
                    var target_piece = board.getPiece(target_pos);
                    if (target_piece == null) {
                        moves.add(new ChessMove(myPosition, target_pos, null));
                    } else {
                        if (target_piece.color != this.color) {
                            moves.add(new ChessMove(myPosition, target_pos, null));
                        }
                        break;
                    }
                }
            }
        }
        return moves;
    }

    private HashSet<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        HashSet<ChessMove> moves = new HashSet<ChessMove>();

        for (int row_off = -1; row_off <= 1; row_off++) {
            for (int col_off = -1; col_off <= 1; col_off++) {
                if (row_off == 0 && col_off == 0) {
                    continue;
                }
                int new_row = row + row_off;
                int new_col = col + col_off;
                var target_pos = new ChessPosition(new_row, new_col);
                if (!board.positionValid(target_pos)) {
                    continue;
                }
                var target_piece = board.getPiece(target_pos);
                if (target_piece == null || target_piece.color != this.color) {
                    moves.add(new ChessMove(myPosition, target_pos, null));
                }
            }
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
