package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.*;
import exception.ResponseException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import models.AuthData;
import org.jetbrains.annotations.NotNull;
import requests.*;
import response.CreateGameResponse;
import response.ListGamesResponse;
import service.AdminService;
import service.GameService;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin server;
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


    }

    private void clear(@NotNull Context ctx) throws Exception {
        adminService.clear();
        ctx.result("{}");
    }

    private void register(@NotNull Context ctx) throws Exception {

        var req = serializer.fromJson(ctx.body(), RegisterRequest.class);
        AuthData authData = userService.register(req);

        var res = Map.of("username", authData.username(), "authToken", authData.authToken());
        ctx.result(serializer.toJson(res));
    }

    private void login(@NotNull Context ctx) throws Exception {
        var req = serializer.fromJson(ctx.body(), Map.class);
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        AuthData authData = userService.login(new LoginRequest(username, password));

        var res = Map.of("username", authData.username(), "authToken", authData.authToken());
        ctx.result(serializer.toJson(res));
    }

    private void logout(@NotNull Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        userService.logout(new LogoutRequest(authToken));
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

    private void databaseExceptionHandler(@NotNull DataAccessException e, @NotNull Context ctx) {
        ctx.status(500);
        ctx.result(new ResponseException("Error: database", 500).toJson());
    }

    private void exceptionHandler(@NotNull ResponseException e, @NotNull Context ctx) {
        ctx.status(e.getStatusCode());
        ctx.result(e.toJson());
    }


    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
