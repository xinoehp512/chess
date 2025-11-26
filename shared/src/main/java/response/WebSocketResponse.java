package response;

import chess.ChessGame;
import models.AuthData;
import models.GameData;

public record WebSocketResponse(GameData gameData, AuthData auth) {
    public ChessGame game() {
        return gameData.game();
    }

    public String username() {
        return auth.username();
    }

    public ChessGame.TeamColor playerColor() {
        return gameData.getColorByUsername(auth.username());
    }
}
