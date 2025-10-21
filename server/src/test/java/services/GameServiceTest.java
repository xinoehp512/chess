package services;

import dataaccess.*;
import models.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.CreateGameRequest;
import requests.RegisterRequest;
import requests.ResponseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GameServiceTest {
    GameService gameService;
    GameDAO gameDAO;
    AuthDAO authDAO;
    private AuthData authData;

    @BeforeEach
    void initGameService() throws ResponseException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);
        var userService = new UserService(new MemoryUserDAO(), authDAO);
        authData = userService.register(new RegisterRequest("xinoehp512", "password", "e@e.com"));
    }

    @Test
    void clear() {
    }

    @Test
    void createGame() throws ResponseException {
        String gameName = "Game 1";
        var response = gameService.createGame(new CreateGameRequest(gameName), authData.authToken());
        assertNotNull(response);
        assertEquals(gameName, gameDAO.getGame(response.gameID()).gameName());
    }

}