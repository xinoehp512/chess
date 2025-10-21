package services;


import dataaccess.GameDAO;
import requests.CreateGameRequest;
import response.CreateGameResponse;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public void clear() {
    }

    public CreateGameResponse createGame(CreateGameRequest req, String authToken) {
        return null;
    }
}
