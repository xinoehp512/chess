package services;


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

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private int id = 1;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clear() {
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest, String authToken) throws ResponseException {
        createGameRequest.assertGood();
        verifyAuth(authToken);
        int gameID = generateGameID();
        GameData gameData = new GameData(gameID, null, null, createGameRequest.gameName(), null);
        gameDAO.insertGame(gameData);
        return new CreateGameResponse(gameID);
    }

    private AuthData verifyAuth(String authToken) throws ResponseException {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new ResponseException("Error: unauthorized", 401);
        }
        return auth;
    }

    public ListGamesResponse listGames(String authToken) {
        return null;
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


    private int generateGameID() {
        return id++;
    }
}
