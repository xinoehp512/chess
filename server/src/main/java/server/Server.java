package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import models.AuthData;
import org.jetbrains.annotations.NotNull;
import requests.RegisterRequest;

import java.util.Map;

public class Server {

    private final Javalin server;
    private final GameService gameService;
    private final UserService userService;

    public Server() {
        gameService = new GameService();
        userService = new UserService();

        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", this::register);


    }

    private void register(@NotNull Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        String email = (String) req.get("email");
        AuthData authData = userService.register(new RegisterRequest(username,password,email));


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
