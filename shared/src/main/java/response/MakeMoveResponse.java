package response;

public record MakeMoveResponse(chess.ChessGame game, chess.ChessGame.TeamColor playerColor,
                               String username, String moveStr) {
}
