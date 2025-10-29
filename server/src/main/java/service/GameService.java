package service;


import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import models.AuthData;
import models.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.ResponseException;
import response.CreateGameResponse;
import response.ListGamesResponse;

import java.util.List;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest, String authToken) throws ResponseException {
        createGameRequest.assertGood();
        verifyAuth(authToken);
        GameData gameData = new GameData(0, null, null, createGameRequest.gameName(), null);
        int gameID = gameDAO.insertGame(gameData);
        return new CreateGameResponse(gameID);
    }

    private AuthData verifyAuth(String authToken) throws ResponseException {
        AuthData auth;
        try {
            auth = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new ResponseException("Error: database", 500);
        }
        if (auth == null) {
            throw new ResponseException("Error: unauthorized", 401);
        }
        return auth;
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        verifyAuth(authToken);
        List<GameData> games = gameDAO.getAll();
        return new ListGamesResponse(games);
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws ResponseException {
        joinGameRequest.assertGood();
        var auth = verifyAuth(authToken);
        var game = gameDAO.getGame(joinGameRequest.gameID());
        if (game == null) {
            throw new ResponseException("Error: bad request", 400);
        }
        GameData updatedGame = addPlayer(game, auth.username(), joinGameRequest.playerColor());
        try {
            gameDAO.updateGame(updatedGame);
        } catch (DataAccessException e) {
            throw new RuntimeException("Congratulations, you *really* broke it.");
        }
    }

    private GameData addPlayer(GameData game, String username, String playerColor) throws ResponseException {
        ChessGame.TeamColor color = switch (playerColor) {
            case "WHITE" -> ChessGame.TeamColor.WHITE;
            case "BLACK" -> ChessGame.TeamColor.BLACK;
            default -> throw new ResponseException("Error: bad request", 400);
        };
        String existingUsername = game.getUsernameByColor(color);
        if (existingUsername != null) {
            throw new ResponseException("Error: already taken", 403);
        }
        return game.addColor(color, username);
    }

}
