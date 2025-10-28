package service;

import dataaccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.CreateGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import requests.ResponseException;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private AdminService adminService;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();
        adminService = new AdminService(gameDAO, authDAO, userDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    void clear() throws ResponseException, DataAccessException {
        String[] usernames = {"user1", "user2", "user3"};
        String[] passwords = {"pw1", "pw2", "pw3"};
        String[] emails = {"em1@e.com", "em2@e.com", "em3@e.com"};
        String[] games = {"g1", "g2", "g3"};
        String[] auths = new String[3];
        for (var i = 0; i < 3; i++) {
            userService.register(new RegisterRequest(usernames[i], passwords[i], emails[i]));
            auths[i] = userService.login(new LoginRequest(usernames[i], passwords[i])).authToken();
            gameService.createGame(new CreateGameRequest(games[i]), auths[i]);
        }
        adminService.clear();
        for (var i = 0; i < 3; i++) {
            assertNull(authDAO.getAuth(auths[i]));
            assertNull(userDAO.getUser(usernames[i]));
            assertEquals(0, gameDAO.getAll().size());
        }

    }

    @Test
    void clearEmpty() {
        assertDoesNotThrow(() -> adminService.clear());
    }
}