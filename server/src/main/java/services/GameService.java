package services;


import dataaccess.AuthDAO;
import dataaccess.GameDAO;
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

    public CreateGameResponse createGame(CreateGameRequest req, String authToken) throws ResponseException {
        if (authDAO.getAuth(authToken) == null) {
            throw new ResponseException("Error: unauthorized", 401);
        }
        int gameID = generateGameID();
        GameData gameData = new GameData(gameID, null, null, req.gameName(), null);
        gameDAO.insertGame(gameData);
        return new CreateGameResponse(gameID);
    }

    public ListGamesResponse listGames(String authToken) {
        return null;
    }

    public void joinGame(JoinGameRequest req, String authToken) {
    }


    private int generateGameID() {
        return id++;
    }
}
