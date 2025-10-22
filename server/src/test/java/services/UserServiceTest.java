package services;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import requests.ResponseException;

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
        var auth = userService.login(new LoginRequest(username, password));
        assertNotNull(auth.authToken());
        assertEquals(username, auth.username());
    }

    @Test
    void loginWrongPassword() throws ResponseException {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        assertThrows(ResponseException.class, () -> userService.login(new LoginRequest(username, "hackerpassword")));
    }

    @Test
    void loginWrongUsername() throws ResponseException {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        assertThrows(ResponseException.class, () -> userService.login(new LoginRequest("xineohp512", password)));
    }

    @Test
    void loginTwice() throws ResponseException {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        var auth = userService.login(new LoginRequest(username, password));
        var auth2 = userService.login(new LoginRequest(username, password));
        assertTrue(authDAO.authIsValid(auth));
        assertTrue(authDAO.authIsValid(auth2));
        assertNotEquals(auth.authToken(), auth2.authToken());
    }

    @Test
    void logout() throws ResponseException {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        var auth = userService.login(new LoginRequest(username, password));
        userService.logout(new LogoutRequest(auth.authToken()));
        assertFalse(authDAO.authIsValid(auth));
    }

    @Test
    void logoutTwice() throws ResponseException {
        String username = "xinoehp512";
        String password = "$ecureP4ssw0rd";
        String email = "xinoehp512@gmail.com";
        userService.register(new RegisterRequest(username, password, email));
        var auth = userService.login(new LoginRequest(username, password));
        userService.logout(new LogoutRequest(auth.authToken()));
        assertThrows(ResponseException.class, () -> userService.logout(new LogoutRequest(auth.authToken())));
    }

//    @Test
//    void clear() throws ResponseException {
//        String[] usernames = {"user1", "user2", "user3"};
//        String[] passwords = {"pw1", "pw2", "pw3"};
//        String[] emails = {"em1@e.com", "em2@e.com", "em3@e.com"};
//        String[] auths = new String[3];
//        for (var i = 0; i < 3; i++) {
//            userService.register(new RegisterRequest(usernames[i], passwords[i], emails[i]));
//            auths[i] = userService.login(new LoginRequest(usernames[i], passwords[i])).authToken();
//        }
//        userService.clear();
//        for (var i = 0; i < 3; i++) {
//            int finalI = i;
//            assertThrows(ResponseException.class,
//                    () -> userService.login(new LoginRequest(usernames[finalI], passwords[finalI])));
//            assertNull(authDAO.getAuth(auths[i]));
//        }
//    }
}