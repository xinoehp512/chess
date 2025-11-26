package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.*;
import exception.ResponseException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import org.jetbrains.annotations.NotNull;
import requests.*;
import response.*;
import server.websocket.ConnectionManager;
import service.AdminService;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class Server {

    private final Javalin server;
    private final ConnectionManager connections;
    private final GameService gameService;
    private final UserService userService;
    private final AdminService adminService;
    private final Gson serializer = new Gson();

    public Server() {
        AuthDAO authDAO;
        UserDAO userDAO;
        GameDAO gameDAO;
        try {
            authDAO = new DatabaseAuthDAO();
            userDAO = new DatabaseUserDAO();
            gameDAO = new DatabaseGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        gameService = new GameService(gameDAO, authDAO);
        userService = new UserService(userDAO, authDAO);
        adminService = new AdminService(gameDAO, authDAO, userDAO);

        connections = new ConnectionManager();

        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.delete("db", this::clear);
        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.get("game", this::listGames);
        server.post("game", this::createGame);
        server.put("game", this::joinGame);
        server.exception(DataAccessException.class, this::databaseExceptionHandler);
        server.exception(ResponseException.class, this::exceptionHandler);
        server.ws("/ws", ws -> {
            ws.onConnect(this::handleConnect);
            ws.onMessage(this::handleMessage);
            ws.onClose(this::handleClose);
        });
        server.exception(Exception.class, (e, context) -> e.printStackTrace());


    }

    /* Endpoints */
    private void clear(@NotNull Context ctx) throws Exception {
        adminService.clear();
        ctx.result("{}");
    }

    private void register(@NotNull Context ctx) throws Exception {

        var req = serializer.fromJson(ctx.body(), RegisterRequest.class);
        LoginResponse registerResponse = userService.register(req);

        ctx.result(serializer.toJson(registerResponse));
    }

    private void login(@NotNull Context ctx) throws Exception {
        var loginRequest = serializer.fromJson(ctx.body(), LoginRequest.class);
        LoginResponse loginResponse = userService.login(loginRequest);
        ctx.result(serializer.toJson(loginResponse));
    }

    private void logout(@NotNull Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        userService.logout(authToken);
        ctx.result("{}");
    }

    private void listGames(@NotNull Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        ListGamesResponse res = gameService.listGames(authToken);
        ctx.result(serializer.toJson(res));
    }

    private void createGame(@NotNull Context ctx) throws Exception {
        var req = serializer.fromJson(ctx.body(), CreateGameRequest.class);
        String authToken = ctx.header("authorization");
        CreateGameResponse res = gameService.createGame(req, authToken);
        ctx.result(serializer.toJson(res));
    }

    private void joinGame(@NotNull Context ctx) throws Exception {
        try {
            String authToken = ctx.header("authorization");
            JoinGameRequest req = serializer.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(req, authToken);
            ctx.result("{}");
        } catch (JsonSyntaxException e) {
            throw new ResponseException("Error: bad request", 400);
        }
    }

    /* Websocket Handlers */
    private void handleConnect(WsConnectContext wsConnectContext) {
        System.out.println("Websocket Connected.");
        wsConnectContext.enableAutomaticPings();
    }

    private void handleMessage(WsMessageContext wsMessageContext) {
        UserGameCommand command = serializer.fromJson(wsMessageContext.message(),
                UserGameCommand.class);
        try {
            try {
                switch (command.getCommandType()) {
                    case CONNECT -> {
                        WebSocketResponse response = gameService.enterGame(command);
                        int gameID = response.gameID();
                        connections.add(gameID, wsMessageContext.session);
                        connections.send(new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, response.game()), wsMessageContext.session);
                        String message = response.username() + " joined as " +
                                         switch (response.playerColor()) {
                                             case WHITE -> "White.";
                                             case BLACK -> "Black.";
                                             case null -> "an observer.";
                                         };
                        connections.broadcast(gameID,
                                new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message), wsMessageContext.session);

                    }
                    case MAKE_MOVE -> {
                        WebSocketResponse response = gameService.makeMove(command);
                        int gameID = response.gameID();
                        connections.broadcast(gameID,new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, response.game()), null);

                        String message =
                                response.username() + " moved " + command.getMove().toString();
                        connections.broadcast(gameID,new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message), wsMessageContext.session);
                        String notification = switch (response.game().getGameState()) {
                            case CHECK -> "Check!";
                            case CHECKMATE -> "Checkmate!";
                            case STALEMATE -> "Stalemate!";
                            case NONE -> null;
                        };
                        if (notification != null) {
                            connections.broadcast(gameID,new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notification), null);
                        }
                    }
                    case LEAVE -> {
                        WebSocketResponse response = gameService.leaveGame(command);
                        int gameID = response.gameID();
                        connections.broadcast(gameID,new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                                response.username() + " left the game."), wsMessageContext.session);
                        connections.remove(gameID,wsMessageContext.session);
                    }
                    case RESIGN -> {
                        WebSocketResponse response = gameService.resignGame(command);
                        int gameID = response.gameID();
                        connections.broadcast(gameID,new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                                response.username() + " resigned."), null);
                    }
                }
            } catch (ResponseException e) {
                connections.send(new ServerMessage(ServerMessage.ServerMessageType.ERROR,
                        e.getMessage()), wsMessageContext.session);
            }
        } catch (IOException | DataAccessException e) {
            e.printStackTrace();
        }
    }

    private void handleClose(WsCloseContext wsCloseContext) {
        System.out.println("Websocket Closed.");
    }

    /* Exception Handlers */
    private void databaseExceptionHandler(@NotNull DataAccessException e, @NotNull Context ctx) {
        ctx.status(500);
        ctx.result(new ResponseException("Error: database", 500).toJson());
    }

    private void exceptionHandler(@NotNull ResponseException e, @NotNull Context ctx) {
        ctx.status(e.getStatusCode());
        ctx.result(e.toJson());
    }

    /* Server Functions */
    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }


}
