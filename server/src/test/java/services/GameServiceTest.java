package services;

import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.CreateGameRequest;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    GameService gameService;
    GameDAO gameDao;

    @BeforeEach
    void initGameService() {
        gameDao = new MemoryGameDAO();
        gameService = new GameService(gameDao);
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
        assertEquals(gameName, gameDao.getGame(response.gameID()).gameName());
    }
}