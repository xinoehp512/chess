package dataaccess;

import models.AuthData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.CreateGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;

import static org.junit.jupiter.api.Assertions.*;

class AuthDAOTest {


    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    void insertAuth(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        var authData = new AuthData("token", "username");
        authDAO.insertAuth(authData);
        assertEquals(authData,authDAO.getAuth(authData.authToken()));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    void deleteAuth(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        var authData = new AuthData("token", "username");
        authDAO.insertAuth(authData);
        authDAO.deleteAuth(authData.authToken());
        assertNull(authDAO.getAuth(authData.authToken()));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    void clear(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        String[] usernames = {"user1", "user2", "user3"};
        String[] tokens = {"pw1", "pw2", "pw3"};
        AuthData[] auths = new AuthData[3];
        for (var i = 0; i < 3; i++) {
            auths[i]= new AuthData(tokens[i],usernames[i]);
            authDAO.insertAuth(auths[i]);
        }
        authDAO.clear();
        for (var i = 0; i < 3; i++) {
            assertNull(authDAO.getAuth(auths[i].authToken()));
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    void validAuthIsValid(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        var authData = new AuthData("token", "username");
        authDAO.insertAuth(authData);
        assertTrue(authDAO.authIsValid(authData));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryAuthDAO.class})
    void invalidAuthIsInvalid(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        var authData = new AuthData("token", "username");
        assertFalse(authDAO.authIsValid(authData));
    }
}