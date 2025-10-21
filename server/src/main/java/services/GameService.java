package services;


import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
    }

    public void clear() {
    }

    public CreateGameResponse createGame(CreateGameRequest req, String authToken) {
        return null;
    }

    public ListGamesResponse listGames(String authToken) {
        return null;
    }

    public void joinGame(JoinGameRequest req, String authToken) {
    }
}
