package services;

import dataaccess.*;
import models.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.RegisterRequest;
import requests.ResponseException;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    GameService gameService;
    GameDAO gameDAO;
    AuthDAO authDAO;
    private AuthData authData;
    private AuthData authDataOtherUser;

    @BeforeEach
    void initGameService() throws ResponseException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);
        var userService = new UserService(new MemoryUserDAO(), authDAO);
        authData = userService.register(new RegisterRequest("xinoehp512", "password", "e@e.com"));
        authDataOtherUser = userService.register(new RegisterRequest("Brain", "zzzzz", "z@z.com"));
    }

    @Test
    void clear() {
    }

    @Test
    void createGame() throws ResponseException {
        String gameName = "Game 1";
        var response = gameService.createGame(new CreateGameRequest(gameName),
                authData.authToken());
        assertNotNull(response);
        assertEquals(gameName, gameDAO.getGame(response.gameID()).gameName());
    }

    @Test
    void listGames() {
    }

    @Test
    void joinGameWhite() throws ResponseException {
        String gameName = "Game 1";
        var response = gameService.createGame(new CreateGameRequest(gameName),
                authData.authToken());
        var gameID = response.gameID();
        gameService.joinGame(new JoinGameRequest("WHITE", gameID), authData.authToken());
        var game = gameDAO.getGame(gameID);
        assertNull(game.blackUsername());
        assertEquals(authData.username(), game.whiteUsername());
    }

    @Test
    void joinGameBlack() throws ResponseException {
        String gameName = "Game 1";
        var response = gameService.createGame(new CreateGameRequest(gameName),
                authData.authToken());
        var gameID = response.gameID();
        gameService.joinGame(new JoinGameRequest("BLACK", gameID), authData.authToken());
        var game = gameDAO.getGame(gameID);
        assertNull(game.whiteUsername());
        assertEquals(authData.username(), game.blackUsername());
    }

    @Test
    void joinGameBadColor() throws ResponseException {
        String gameName = "Game 1";
        var response = gameService.createGame(new CreateGameRequest(gameName),
                authData.authToken());
        var gameID = response.gameID();
        assertThrows(ResponseException.class, () -> gameService.joinGame(new JoinGameRequest(
                "BLACK ", gameID), authData.authToken()));

    }
}