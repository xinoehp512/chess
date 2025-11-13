package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import models.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.LoginRequest;
import requests.RegisterRequest;
import exception.ResponseException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    UserService userService;
    AuthDAO authDAO;

    @BeforeEach
    void initUserService() {
        authDAO = new MemoryAuthDAO();
        var userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerNewUser() throws Exception {
        String username = "xinoehp512";
        var auth = userService.register(new RegisterRequest(username, "$ecureP4ssw0rd",
                "xinoehp512@gmail.com"));
        assertNotNull(auth.authToken());
        assertEquals(username, auth.username());
    }

    @Test
    void registerDuplicateUsername() throws Exception {
        String username = "Bob";
        userService.register(new RegisterRequest(username, "password", "bob@gmail.com"));
        assertThrows(ResponseException.class,
                () -> userService.register(new RegisterRequest(username, "12345",
                        "bob@yahoo.com")));
    }

    @Test
    void login() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        var auth = userService.login(new LoginRequest(username, password));
        assertNotNull(auth.authToken());
        assertEquals(username, auth.username());
    }

    @Test
    void loginWrongPassword() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        assertThrows(ResponseException.class, () -> userService.login(new LoginRequest(username,
                "hackerpassword")));
    }

    @Test
    void loginWrongUsername() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        assertThrows(ResponseException.class, () -> userService.login(new LoginRequest(
                "xineohp512", password)));
    }

    @Test
    void loginTwice() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        AuthData auth = userService.login(new LoginRequest(username, password)).getAuthData();
        AuthData auth2 = userService.login(new LoginRequest(username, password)).getAuthData();
        assertTrue(authDAO.authIsValid(auth));
        assertTrue(authDAO.authIsValid(auth2));
        assertNotEquals(auth.authToken(), auth2.authToken());
    }

    @Test
    void logout() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        AuthData auth = userService.login(new LoginRequest(username, password)).getAuthData();
        userService.logout(auth.authToken());
        assertFalse(authDAO.authIsValid(auth));
    }

    @Test
    void logoutTwice() throws Exception {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        var auth = userService.login(new LoginRequest(username, password));
        userService.logout(auth.authToken());
        assertThrows(ResponseException.class,
                () -> userService.logout(auth.authToken()));
    }
}