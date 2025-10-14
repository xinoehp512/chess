package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import models.AuthData;
import org.jetbrains.annotations.NotNull;
import requests.AlreadyTakenException;
import requests.RegisterRequest;
import requests.ResponseException;

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
        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", this::register);
        server.exception(ResponseException.class,this::exceptionHandler);


    }

    private void exceptionHandler(@NotNull ResponseException e, @NotNull Context ctx) {
        ctx.status(e.getStatusCode());
        ctx.result(e.toJson());
    }

    private void register(@NotNull Context ctx) throws AlreadyTakenException {

        var req = serializer.fromJson(ctx.body(), Map.class);
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        String email = (String) req.get("email");
        AuthData authData = userService.register(new RegisterRequest(username, password, email));


        var res = Map.of("username", authData.username(), "authToken", authData.authToken());
        ctx.result(serializer.toJson(res));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
