package server;

import com.google.gson.Gson;
import exception.ResponseException;
import models.AuthData;
import models.GameData;
import requests.*;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginResponse;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.BodyHandlers;

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


    private HttpRequest buildRequest(String method, String path) {
        return buildRequest(method, path, null, null);
    }

    private HttpRequest buildRequest(String method, String path, Request body) {
        return buildRequest(method, path, body, null);
    }

    private HttpRequest buildRequest(String method, String path, String authToken) {
        return buildRequest(method, path, null, authToken);
    }

    private HttpRequest buildRequest(String method, String path, Request body, String authToken) {
        var request = HttpRequest.newBuilder().uri(URI.create(
                serverUrl + path)).method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if (authToken != null) {
            request.setHeader("authorization", authToken);
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Request request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage(), 500);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException("Other failure: " + status, status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
