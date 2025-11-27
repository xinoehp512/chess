package ui;

import chess.ChessPiece;

import java.util.Set;

public interface ChessUI {
    void run();

    void showGame();

    void showError(String errorMessage);

    void showNotification(String message);

    ChessPiece.PieceType promptPieceSelection(Set<ChessPiece.PieceType> pieceTypes);
}
