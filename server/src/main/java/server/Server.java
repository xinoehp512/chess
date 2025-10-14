package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import models.AuthData;
import org.jetbrains.annotations.NotNull;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import requests.ResponseException;
import services.GameService;
import services.UserService;

import java.util.Map;

public class Server {

    private final Javalin server;
    private final GameService gameService;
    private final UserService userService;
    private final Gson serializer = new Gson();

    public Server() {
        gameService = new GameService();
        userService = new UserService();

        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.delete("db", this::clear);
        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.get("game", this::listGames);
        server.post("game", this::createGame);
        server.put("game", this::joinGame);
        server.exception(ResponseException.class, this::exceptionHandler);


    }

    private void clear(@NotNull Context ctx) {
        gameService.clear();
        userService.clear();
        ctx.result("{}");
    }

    private void register(@NotNull Context ctx) throws ResponseException {

        var req = serializer.fromJson(ctx.body(), RegisterRequest.class);
        AuthData authData = userService.register(req);

        var res = Map.of("username", authData.username(), "authToken", authData.authToken());
        ctx.result(serializer.toJson(res));
    }

    private void login(@NotNull Context ctx) throws ResponseException {
        var req = serializer.fromJson(ctx.body(), Map.class);
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        AuthData authData = userService.login(new LoginRequest(username,password));

        var res = Map.of("username", authData.username(), "authToken", authData.authToken());
        ctx.result(serializer.toJson(res));
    }

    private void logout(@NotNull Context ctx) throws ResponseException {
        String authToken = serializer.fromJson(ctx.header("authorization"), String.class);
        userService.logout(new LogoutRequest(authToken));
        ctx.result("{}");
    }

    private void listGames(@NotNull Context ctx) {

    }

    private void createGame(@NotNull Context ctx) {

    }

    private void joinGame(@NotNull Context ctx) {

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
