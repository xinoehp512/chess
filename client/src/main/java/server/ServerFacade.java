package server;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import models.GameData;
import requests.*;
import response.CreateGameResponse;
import response.ListGamesResponse;
import response.LoginResponse;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

public class ServerFacade extends Endpoint {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    private Session session;

    public ServerFacade(String url, NotificationObserver notificationObserver) throws ResponseException {
        serverUrl = url;

        try {
            URI webSocketURI = new URI(serverUrl.replace("http", "ws") + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, webSocketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    notificationObserver.notify(serverMessage);
                }
            });
        } catch (URISyntaxException | DeploymentException | IOException e) {
            throw new ResponseException(e.getMessage(), 500);
        }
    }

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public LoginResponse login(LoginRequest request) throws ResponseException {
        return performRequest("POST", "/session", request, null, LoginResponse.class);
    }

    public LoginResponse register(RegisterRequest request) throws ResponseException {
        return performRequest("POST", "/user", request, null, LoginResponse.class);
    }

    public void logout(String authToken) throws ResponseException {
        performRequest("DELETE", "/session", null, authToken, null);
    }

    public CreateGameResponse createGame(CreateGameRequest request, String authToken) throws ResponseException {
        return performRequest("POST", "/game", request, authToken, CreateGameResponse.class);
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        return performRequest("GET", "/game", null, authToken, ListGamesResponse.class);
    }

    public void joinGame(JoinGameRequest request, String authToken) throws ResponseException {
        performRequest("PUT", "/game", request, authToken, null);
    }

    public GameData getGame(int gameID, String authToken) throws ResponseException {
        List<GameData> gameDataList = listGames(authToken).games();
        for (var gameData : gameDataList) {
            if (gameData.gameID() == gameID) {
                return gameData;
            }
        }
        return null;
    }

    public void clear() throws ResponseException {
        performRequest("DELETE", "/db", null, null, null);
    }

    private <T> T performRequest(String method, String path, Request body, String authToken,
                                 Class<T> responseClass) throws ResponseException {
        var request = buildRequest(method, path, body, authToken);
        var response = sendRequest(request);
        return handleResponse(response, responseClass);
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
        } catch (ConnectException ex) {
            throw new ResponseException("Failed to Connect", 500);
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

    private void sendCommand(UserGameCommand command) throws ResponseException {
        try {
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException e) {
            throw new ResponseException(e.getMessage(), 500);
        }
    }

    public void connect(int gameID, String authToken) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void leave(int gameID, String authToken) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }

    public void move(int gameID, String authToken, ChessMove move) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID,
                move));
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }
}
