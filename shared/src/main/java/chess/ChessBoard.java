package chess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] board = new ChessPiece[8][8];
    private static final String[] START_TEMPLATE = {
            "RNBQKBNR",
            "PPPPPPPP",
            "        ",
            "        ",
            "        ",
            "        ",
            "pppppppp",
            "rnbqkbnr",
    };
    private ChessPosition passant_square;
    private Map<ChessGame.TeamColor,Boolean> can_castle = new HashMap<>( Map.of(
            ChessGame.TeamColor.WHITE,false,
            ChessGame.TeamColor.BLACK,false
    ));

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
     * Executes the specified move.
     *
     * @param move the move to execute
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var startPosition = move.getStartPosition();
        var endPosition = move.getEndPosition();
        var startX = startPosition.getColumn()-1;
        var startY = startPosition.getRow()-1;
        var endX = endPosition.getColumn()-1;
        var endY = endPosition.getRow()-1;
        var piece = board[startY][startX];
        if(piece==null){
            throw new InvalidMoveException();
        }
        board[startY][startX]=null;
        board[endY][endX]=piece;
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

        can_castle.put(ChessGame.TeamColor.WHITE,true);
        can_castle.put(ChessGame.TeamColor.BLACK,true);
        passant_square =null;
        for (int row = 0; row < 8; row++) {
            String pieceRow = ChessBoard.START_TEMPLATE[row];
            for (int col = 0; col < 8; col++) {
                char pieceChar = pieceRow.charAt(col);
                ChessPiece piece;

                if (pieceChar == ' ') {
                    piece = null;
                } else {
                    chess.ChessGame.TeamColor color = Character.isUpperCase(pieceChar) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                    ChessPiece.PieceType type = switch (Character.toLowerCase(pieceChar)) {
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
        return Objects.deepEquals(board, that.board) && Objects.equals(passant_square, that.passant_square) && Objects.equals(can_castle, that.can_castle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.deepHashCode(board), passant_square, can_castle);
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
                char pieceChar = switch (piece.type) {
                    case ChessPiece.PieceType.PAWN -> 'p';
                    case ChessPiece.PieceType.BISHOP -> 'b';
                    case ChessPiece.PieceType.KNIGHT -> 'n';
                    case ChessPiece.PieceType.ROOK -> 'r';
                    case ChessPiece.PieceType.QUEEN -> 'q';
                    case ChessPiece.PieceType.KING -> 'k';
                };
                if (piece.color == ChessGame.TeamColor.WHITE) {
                    pieceChar = Character.toUpperCase(pieceChar);
                }
                builder.append(pieceChar);
            }
            builder.append("\n");
        }
        return builder.toString();
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
