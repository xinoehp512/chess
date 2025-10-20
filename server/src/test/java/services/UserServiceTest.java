package services;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import models.AuthData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.RegisterRequest;
import requests.ResponseException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    UserService userService;

    @BeforeEach
    void initUserService() {
        var authDAO = new MemoryAuthDAO();
        var userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerNewUser() throws ResponseException {
        String username = "xinoehp512";
        var auth = userService.register(new RegisterRequest(username, "$ecureP4ssw0rd", "xinoehp512@gmail.com"));
        assertNotNull(auth.authToken());
        assertEquals(username, auth.username());
    }

    @Test
    void registerDuplicateUser() throws ResponseException {
    }

    @Test
    void login() {
    }

    @Test
    void logout() {
    }

    @Test
    void clear() {
    }


}