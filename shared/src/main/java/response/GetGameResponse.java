package response;

import chess.ChessGame;
import models.GameData;

public record GetGameResponse(GameData game, ChessGame.TeamColor playerColor, String username) {
}
