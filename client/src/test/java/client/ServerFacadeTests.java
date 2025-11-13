package client;

import exception.ResponseException;
import models.AuthData;
import models.GameData;
import org.junit.jupiter.api.*;
import requests.*;
import server.Server;
import server.ServerFacade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Nested
    class ServerFacadeUserTests {
        @BeforeEach
        void init() {
            try {
                serverFacade.clear();
            } catch (ResponseException e) {
                System.out.println("Server Clear Failed!");
            }
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
                    () -> serverFacade.register(new RegisterRequest(username, "12345",
                    "bob@yahoo" + ".com")));
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
            assertThrows(ResponseException.class,
                    () -> serverFacade.login(new LoginRequest(username, "hackerpassword")));
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
    }


    @Nested
    class ServerFacadeGameTests {
        private AuthData authData;
        private AuthData authDataOtherUser;

        @BeforeEach
        void initGameTests() throws Exception {
            try {
                serverFacade.clear();
            } catch (ResponseException e) {
                System.out.println("Server Clear Failed!");
            }
            authData = ServerFacadeTests.serverFacade.register(new RegisterRequest("xinoehp512",
                    "password", "e@e.com")).getAuthData();
            authDataOtherUser = ServerFacadeTests.serverFacade.register(new RegisterRequest(
                    "Brain", "zzzzz", "z@z.com")).getAuthData();

        }

        @Test
        void createGame() throws Exception {
            String gameName = "Game 1";
            var response =
                    ServerFacadeTests.serverFacade.createGame(new CreateGameRequest(gameName),
                            authData.authToken());
            assertNotNull(response);
            assertEquals(gameName,
                    serverFacade.getGame(response.gameID(), authData.authToken()).gameName());
        }

        @Test
        void createBadAuth() {
            String gameName = "Game 1";
            assertThrows(ResponseException.class,
                    () -> ServerFacadeTests.serverFacade.createGame(new CreateGameRequest(gameName), "bad"));
        }

        @Test
        void listGames() throws Exception {
            var gameNames = Set.of("Game 1", "Game 2", "Game 3");
            for (var name : gameNames) {
                ServerFacadeTests.serverFacade.createGame(new CreateGameRequest(name),
                        authData.authToken());
            }

            List<GameData> games =
                    ServerFacadeTests.serverFacade.listGames(authData.authToken()).games();
            HashSet<String> listedGameNames = new HashSet<>();
            for (var gameData : games) {
                listedGameNames.add(gameData.gameName());
            }
            assertEquals(gameNames, listedGameNames);
        }

        @Test
        void listGamesBadAuth() {
            assertThrows(ResponseException.class,
                    () -> ServerFacadeTests.serverFacade.createGame(new CreateGameRequest(
                    "Game " + "1"), "bad"));
        }

        @Test
        void joinGameWhite() throws Exception {
            String gameName = "Game 1";
            var response =
                    ServerFacadeTests.serverFacade.createGame(new CreateGameRequest(gameName),
                            authData.authToken());
            var gameID = response.gameID();
            ServerFacadeTests.serverFacade.joinGame(new JoinGameRequest("WHITE", gameID),
                    authData.authToken());
            var game = serverFacade.getGame(gameID, authData.authToken());
            assertNull(game.blackUsername());
            assertEquals(authData.username(), game.whiteUsername());
        }

        @Test
        void joinGameBlack() throws Exception {
            String gameName = "Game 1";
            var response =
                    ServerFacadeTests.serverFacade.createGame(new CreateGameRequest(gameName),
                            authData.authToken());
            var gameID = response.gameID();
            ServerFacadeTests.serverFacade.joinGame(new JoinGameRequest("BLACK", gameID),
                    authData.authToken());
            var game = serverFacade.getGame(gameID, authData.authToken());
            assertNull(game.whiteUsername());
            assertEquals(authData.username(), game.blackUsername());
        }

        @Test
        void joinGameBadColor() throws Exception {
            String gameName = "Game 1";
            var response =
                    ServerFacadeTests.serverFacade.createGame(new CreateGameRequest(gameName),
                            authData.authToken());
            var gameID = response.gameID();
            assertThrows(ResponseException.class,
                    () -> ServerFacadeTests.serverFacade.joinGame(new JoinGameRequest("BLACK ",
                            gameID), authData.authToken()));

        }

        @Test
        void joinGameColorTaken() throws Exception {
            String gameName = "Game 1";
            var response =
                    ServerFacadeTests.serverFacade.createGame(new CreateGameRequest(gameName),
                            authData.authToken());
            var gameID = response.gameID();
            ServerFacadeTests.serverFacade.joinGame(new JoinGameRequest("WHITE", gameID),
                    authData.authToken());
            assertThrows(ResponseException.class,
                    () -> ServerFacadeTests.serverFacade.joinGame(new JoinGameRequest("WHITE",
                            gameID), authDataOtherUser.authToken()));
        }

        @Test
        void joinGameNoGame() {
            assertThrows(ResponseException.class,
                    () -> ServerFacadeTests.serverFacade.joinGame(new JoinGameRequest("WHITE", 0)
                            , authDataOtherUser.authToken()));
        }
    }
}
