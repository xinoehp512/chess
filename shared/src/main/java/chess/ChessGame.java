package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();

    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    public static TeamColor otherTeam(TeamColor team) {
        return team == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        var moves = board.validMoves(startPosition);
        var piece = board.getPiece(startPosition);
        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && !piece.getHasMoved()) {
            var color = piece.getTeamColor();
            var attackedByOpponent = board.getAttackedBy(otherTeam(color));
            var kingX = startPosition.getColumn();
            var kingY = startPosition.getRow();
            HashSet<ChessPosition> castleCandidates = board.getCastleCandidates(color);
            for (var rookPosition : castleCandidates) {
                var rookX = rookPosition.getColumn();
                var direction = Integer.signum(rookX - kingX);
                var canCastle = true;
                for (int x = kingX; Math.abs(x - rookX) > 0; x += direction) {
                    var position = new ChessPosition(kingY, x);
                    if ((board.getPiece(position) != null && !(x == kingX)) || attackedByOpponent.contains(position)) {
                        canCastle = false;
                        break;
                    }
                }
                if (canCastle) {
                    var newPosition = new ChessPosition(kingY, kingX + direction * 2);
                    var move = new ChessMove(startPosition, newPosition, null);
                    moves.add(move);
                }
            }
        }

        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var startPosition = move.getStartPosition();
        var targetPiece = board.getPiece(startPosition);
        if (targetPiece == null) {
            throw new InvalidMoveException("No piece at " + startPosition);
        }
        var pieceColor = targetPiece.getTeamColor();
        if (pieceColor != currentTurn) {
            throw new InvalidMoveException(targetPiece.getTeamColor() + " cannot move on " + currentTurn + "'s turn.");
        }
        var legalMoves = validMoves(move.getStartPosition());
        if (legalMoves != null && !legalMoves.contains(move)) {
            throw new InvalidMoveException(move + " is invalid.");
        }
        board.makeMove(move);
        if (move.getPromotionPiece() != null) {
            var promotedPiece = new ChessPiece(pieceColor, move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), promotedPiece);
        }
        if (targetPiece.getPieceType() == ChessPiece.PieceType.KING) {
            var kingRow = startPosition.getRow();
            var startX = startPosition.getColumn();
            var endX = move.getEndPosition().getColumn();
            var displacement = startX - endX;
            if (Math.abs(displacement) == 2) {
                var rookCol = displacement > 0 ? 1 : 8;
                var newRookCol = displacement > 0 ? 4 : 6;
                var rookPosition = new ChessPosition(kingRow, rookCol);
                var newRookPosition = new ChessPosition(kingRow, newRookCol);
                var rookMove = new ChessMove(rookPosition, newRookPosition, null);
                board.makeMove(rookMove);
            }
        }

        currentTurn = otherTeam(currentTurn);
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return board.isInCheck(teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return board.isInCheck(teamColor) && board.validMoves(teamColor).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !board.isInCheck(teamColor) && board.validMoves(teamColor).isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
    }
}
