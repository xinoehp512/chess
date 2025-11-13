package client;

import exception.ResponseException;
import models.AuthData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:8080");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    void registerNewUser() throws Exception {
        String username = "xinoehp512";
        var auth = serverFacade.register(new RegisterRequest(username, "$ecureP4ssw0rd",
                "xinoehp512@gmail.com"));
        assertNotNull(auth.authToken());
        assertEquals(username, auth.username());
    }

    @Test
    void registerDuplicateUsername() throws Exception {
        String username = "Bob";
        serverFacade.register(new RegisterRequest(username, "password", "bob@gmail.com"));
        assertThrows(ResponseException.class,
                () -> serverFacade.register(new RegisterRequest(username, "12345", "bob@yahoo" +
                                                                                   ".com")));
    }

    @Test
    void login() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        serverFacade.register(new RegisterRequest(username, password, email));
        var auth = serverFacade.login(new LoginRequest(username, password));
        assertNotNull(auth.authToken());
        assertEquals(username, auth.username());
    }

    @Test
    void loginWrongPassword() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        serverFacade.register(new RegisterRequest(username, password, email));
        assertThrows(ResponseException.class, () -> serverFacade.login(new LoginRequest(username,
                "hackerpassword")));
    }

    @Test
    void loginWrongUsername() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        serverFacade.register(new RegisterRequest(username, password, email));
        assertThrows(ResponseException.class, () -> serverFacade.login(new LoginRequest(
                "xineohp512", password)));
    }

    @Test
    void loginTwice() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        serverFacade.register(new RegisterRequest(username, password, email));
        AuthData auth = serverFacade.login(new LoginRequest(username, password)).getAuthData();
        AuthData auth2 = serverFacade.login(new LoginRequest(username, password)).getAuthData();
        assertTrue(serverFacade.authIsValid(auth));
        assertTrue(serverFacade.authIsValid(auth2));
        assertNotEquals(auth.authToken(), auth2.authToken());
    }

    @Test
    void logout() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        serverFacade.register(new RegisterRequest(username, password, email));
        AuthData auth = serverFacade.login(new LoginRequest(username, password)).getAuthData();
        serverFacade.logout(new LogoutRequest(auth.authToken()));
        assertFalse(serverFacade.authIsValid(auth));
    }

    @Test
    void logoutTwice() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        serverFacade.register(new RegisterRequest(username, password, email));
        var auth = serverFacade.login(new LoginRequest(username, password));
        serverFacade.logout(new LogoutRequest(auth.authToken()));
        assertThrows(ResponseException.class,
                () -> serverFacade.logout(new LogoutRequest(auth.authToken())));
    }

    @Test
    void createGame() {
    }

    @Test
    void listGames() {
    }

    @Test
    void joinGame() {
    }
}
