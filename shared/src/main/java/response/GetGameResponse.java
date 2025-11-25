package response;

import chess.ChessGame;
import models.GameData;

public record GetGameResponse(ChessGame game, ChessGame.TeamColor playerColor, String username) {
}
