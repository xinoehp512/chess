package services;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.LoginRequest;
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
    void registerDuplicateUsername() throws ResponseException {
        String username = "Bob";
        userService.register(new RegisterRequest(username, "password", "bob@gmail.com"));
        assertThrows(ResponseException.class, () -> userService.register(new RegisterRequest(username, "12345", "bob@yahoo.com")));
    }

    @Test
    void login() throws ResponseException {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        var auth = userService.login(new LoginRequest(username,password));
        assertNotNull(auth.authToken());
        assertEquals(username, auth.username());
    }

    @Test
    void logout() {
    }

    @Test
    void clear() {
    }


}