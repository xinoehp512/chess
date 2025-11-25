package models;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public String getUsernameByColor(ChessGame.TeamColor color) {
        return switch (color) {
            case WHITE -> whiteUsername;
            case BLACK -> blackUsername;
        };
    }

    public GameData addColor(ChessGame.TeamColor color, String username) {
        return switch (color) {
            case WHITE -> new GameData(gameID, username, blackUsername, gameName, game);
            case BLACK -> new GameData(gameID, whiteUsername, username, gameName, game);
        };
    }

    public GameData addID(int gameID) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

    public GameData updateGame(ChessGame game) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }
}
