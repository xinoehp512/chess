package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board = new ChessPiece[8][8];
    private static final String[] start_template = {
            "RNBQKBNR",
            "PPPPPPPP",
            "        ",
            "        ",
            "        ",
            "        ",
            "pppppppp",
            "rnbqkbnr",
    };

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int row = 0; row < 8; row++) {
            String pieceRow = ChessBoard.start_template[row];
            for (int col = 0; col < 8; col++) {
                char piece_char = pieceRow.charAt(col);
                ChessPiece piece;

                if (piece_char == ' ') {
                    piece = null;
                } else {
                    chess.ChessGame.TeamColor color = Character.isUpperCase(piece_char) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                    ChessPiece.PieceType type = switch (Character.toLowerCase(piece_char)) {
                        case 'p' -> ChessPiece.PieceType.PAWN;
                        case 'b' -> ChessPiece.PieceType.BISHOP;
                        case 'n' -> ChessPiece.PieceType.KNIGHT;
                        case 'r' -> ChessPiece.PieceType.ROOK;
                        case 'q' -> ChessPiece.PieceType.QUEEN;
                        case 'k' -> ChessPiece.PieceType.KING;
                        default -> null;
                    };
                    piece = new ChessPiece(color, type);
                }
                board[row][col] = piece;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ChessPiece[] row : board) {
            for (ChessPiece piece : row) {
                if (piece == null) {
                    builder.append(" ");
                    continue;
                }
                char piece_char = switch (piece.type) {
                    case ChessPiece.PieceType.PAWN -> 'p';
                    case ChessPiece.PieceType.BISHOP -> 'b';
                    case ChessPiece.PieceType.KNIGHT -> 'n';
                    case ChessPiece.PieceType.ROOK -> 'r';
                    case ChessPiece.PieceType.QUEEN -> 'q';
                    case ChessPiece.PieceType.KING -> 'k';
                };
                if (piece.color == ChessGame.TeamColor.WHITE) {
                    piece_char = Character.toUpperCase(piece_char);
                }
                builder.append(piece_char);
            }
            builder.append("\n");
        }
        return builder.toString();

//        return "ChessBoard{" +
//                "board=" + Arrays.deepToString(board) +
//                '}';
    }

    public ChessPiece[][] getBoard() {
        return board;
    }

    public boolean positionValid(ChessPosition targetPos) {
        int row = targetPos.getRow();
        int col = targetPos.getColumn();
        return 0 < row && row <= 8 && 0 < col && col <= 8;
    }
}
