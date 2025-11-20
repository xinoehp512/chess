package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] board = new ChessPiece[8][8];
    private static final String[] START_TEMPLATE = {"RNBQKBNR", "PPPPPPPP", "        ", "        "
            , "        ", "        ", "pppppppp", "rnbqkbnr",};

    private final List<ChessMove> pastMoves = new ArrayList<>();

    public ChessBoard() {
    }

    public ChessBoard(ChessBoard board) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                var piece = board.board[i][j];
                this.board[i][j] = piece == null ? null : new ChessPiece(piece);
            }
        }
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
        var piece = getPiece(startPosition);
        if (piece == null) {
            throw new InvalidMoveException("No piece at location " + startPosition);
        }
        removePiece(startPosition);
        ChessPiece capturedPiece = removePiece(endPosition);
        if (capturedPiece == null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int backDirection = switch (piece.getTeamColor()) {
                case WHITE -> -1;
                case BLACK -> 1;
            };
            removePiece(new ChessPosition(
                    endPosition.getRow() + backDirection, endPosition.getColumn()));
        }
        if (move.getPromotionPiece() != null) {
            var promotedPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            addPiece(endPosition, promotedPiece);
        } else {
            addPiece(endPosition, piece);
        }
        pastMoves.add(move);
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

    public ChessPiece removePiece(ChessPosition position) {
        var capturedPiece = getPiece(position);
        board[position.getRow() - 1][position.getColumn() - 1] = null;
        return capturedPiece;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int row = 0; row < 8; row++) {
            String pieceRow = ChessBoard.START_TEMPLATE[row];
            for (int col = 0; col < 8; col++) {
                char pieceChar = pieceRow.charAt(col);
                ChessPiece piece;

                if (pieceChar == ' ') {
                    piece = null;
                } else {
                    ChessGame.TeamColor color = Character.isUpperCase(pieceChar) ?
                            ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
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
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.deepHashCode(board));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ChessPiece[] row : board) {
            for (ChessPiece piece : row) {
                if (piece == null) {
                    builder.append(".");
                    continue;
                }
                char pieceChar = piece.getChar();
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

    public HashSet<ChessPosition> getAttackedBy(ChessGame.TeamColor teamColor) {
        HashSet<ChessPosition> piecePositions = getPiecePositionsOf(teamColor);
        HashSet<ChessPosition> attackedPositions = new HashSet<>();
        for (var position : piecePositions) {
            var piece = getPiece(position);
            var attacks = piece.pieceAttacks(this, position);
            for (var attack : attacks) {
                var endPos = attack.getEndPosition();
                attackedPositions.add(endPos);
            }
        }
        return attackedPositions;
    }

    private HashSet<ChessPosition> getPiecePositionsOf(ChessGame.TeamColor teamColor) {
        HashSet<ChessPosition> positions = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                var position = new ChessPosition(i, j);
                var piece = getPiece(position);
                if (piece == null) {
                    continue;
                }
                if (piece.getTeamColor() == teamColor) {
                    positions.add(position);
                }
            }
        }
        return positions;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        ChessGame.TeamColor opponent = ChessGame.otherTeam(teamColor);
        HashSet<ChessPosition> attackedByOpponent = getAttackedBy(opponent);
        for (var position : attackedByOpponent) {
            var attackedPiece = getPiece(position);
            if (attackedPiece != null && attackedPiece.is(teamColor, ChessPiece.PieceType.KING)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        var piece = getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        var allMoves = piece.pieceMoves(this, startPosition);
        var pieceColor = piece.getTeamColor();
        for (var move : allMoves) {
            var testBoard = new ChessBoard(this);
            try {
                testBoard.makeMove(move);
            } catch (InvalidMoveException e) {
                continue;
            }
            if (testBoard.isInCheck(pieceColor)) {
                continue;
            }
            moves.add(move);
        }
        return moves;
    }

    public HashSet<ChessMove> validMoves(ChessGame.TeamColor teamColor) {
        var legalMoves = new HashSet<ChessMove>();
        getPiecePositionsOf(teamColor).forEach(position -> legalMoves.addAll(validMoves(position)));
        return legalMoves;
    }


    public HashSet<ChessPosition> getCastleCandidates(ChessGame.TeamColor color) {
        HashSet<ChessPosition> piecePositions = getPiecePositionsOf(color);
        HashSet<ChessPosition> rookPositions = new HashSet<>();
        for (var position : piecePositions) {
            var piece = getPiece(position);
            if (piece.getPieceType() == ChessPiece.PieceType.ROOK && neverMovedFrom(position)) {
                rookPositions.add(position);
            }
        }
        return rookPositions;
    }

    public boolean neverMovedFrom(ChessPosition position) {
        for (ChessMove move : pastMoves) {
            if (Objects.equals(move.getStartPosition(), position)) {
                return false;
            }
        }
        return true;
    }

    public boolean canEnPassantAt(ChessPosition targetPos) {
        if (pastMoves.isEmpty()) {
            return false;
        }
        ChessMove lastMove = pastMoves.getLast();
        ChessPiece movedPiece = getPiece(lastMove.getEndPosition());
        if (movedPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        }
        if (lastMove.getStartPosition().getColumn() != targetPos.getColumn() ||
            lastMove.getEndPosition().getColumn() != targetPos.getColumn()) {
            return false;
        }
        int yDist = Math.abs(
                lastMove.getStartPosition().getRow() - lastMove.getEndPosition().getRow());
        if (yDist < 2) {
            return false;
        }
        int skippedRow =
                (lastMove.getStartPosition().getRow() + lastMove.getEndPosition().getRow()) / 2;
        return targetPos.getRow() == skippedRow;
    }
}
