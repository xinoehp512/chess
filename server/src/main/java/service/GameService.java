package service;


import chess.ChessGame;
import chess.ChessMove;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import models.AuthData;
import models.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import exception.ResponseException;
import response.CreateGameResponse;
import response.GetGameResponse;
import response.ListGamesResponse;
import response.MakeMoveResponse;
import websocket.commands.UserGameCommand;

import java.util.List;
import java.util.Objects;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest, String authToken) throws ResponseException, DataAccessException {
        createGameRequest.assertGood();
        verifyAuth(authToken);
        GameData gameData = new GameData(0, null, null, createGameRequest.gameName(),
                new ChessGame());
        int gameID;
        gameID = gameDAO.insertGame(gameData);
        return new CreateGameResponse(gameID);
    }


    public ListGamesResponse listGames(String authToken) throws ResponseException,
            DataAccessException {
        verifyAuth(authToken);
        List<GameData> games;
        games = gameDAO.getAll();
        return new ListGamesResponse(games);
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws ResponseException, DataAccessException {
        joinGameRequest.assertGood();
        var auth = verifyAuth(authToken);
        GameData game = getGameData(joinGameRequest.gameID());
        GameData updatedGame = addPlayer(game, auth.username(), joinGameRequest.playerColor());
        try {
            gameDAO.updateGame(updatedGame);
        } catch (DataAccessException e) {
            throw new RuntimeException("Congratulations, you *really* broke it.");
        }
    }

    public GetGameResponse getGame(UserGameCommand command) throws ResponseException,
            DataAccessException {
        var auth = verifyAuth(command.getAuthToken());
        GameData game = getGameData(command.getGameID());
        ChessGame.TeamColor playerColor = null;
        if (Objects.equals(auth.username(), game.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (Objects.equals(auth.username(), game.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        }

        return new GetGameResponse(game.game(), playerColor, auth.username());
    }

    public MakeMoveResponse makeMove(UserGameCommand command) throws ResponseException,
            DataAccessException {
        var auth = verifyAuth(command.getAuthToken());
        GameData game = getGameData(command.getGameID());
        ChessGame.TeamColor playerColor = null;
        if (Objects.equals(auth.username(), game.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (Objects.equals(auth.username(), game.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        }
        if (playerColor == null) {
            throw new ResponseException("Error: can't move as an observer.", 400);
        }
        ChessMove move = command.getMove();
        String moveStr = move.toString();
        return new MakeMoveResponse(game.game(), playerColor, auth.username(), moveStr);
    }

    private GameData getGameData(Integer gameID) throws ResponseException, DataAccessException {
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new ResponseException("Error: bad request", 400);
        }
        return game;
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

    private AuthData verifyAuth(String authToken) throws ResponseException, DataAccessException {
        AuthData auth;
        auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new ResponseException("Error: unauthorized", 401);
        }
        return auth;
    }
}
