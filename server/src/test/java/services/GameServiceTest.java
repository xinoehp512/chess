package services;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.CreateGameRequest;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    GameService gameService;
    GameDAO gameDAO;
    AuthDAO authDAO;

    @BeforeEach
    void initGameService() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    void clear() {
    }

    @Test
    void createGame() {
        var authToken="1234";
        String gameName = "Game 1";
        var response = gameService.createGame(new CreateGameRequest(gameName), authToken);
        assertNotNull(response);
        assertEquals(gameName, gameDAO.getGame(response.gameID()).gameName());
    }
}