package server;

import models.AuthData;
import models.GameData;
import requests.*;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginResponse;

import java.net.http.HttpClient;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        return null;
    }

    public LoginResponse register(RegisterRequest registerRequest) {
        return null;
    }

    public void logout(LogoutRequest logoutRequest) {

    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest, String authToken) {
        return null;
    }

    public ListGamesResponse listGames(String authToken) {
        return null;
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) {

    }

    public boolean authIsValid(AuthData authData) {
        return false;
    }


    public GameData getGame(int gameID) {
        return null;
    }
}
