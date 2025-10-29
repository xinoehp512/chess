package dataaccess;

import models.AuthData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthDAOTest {

    private static List<Class<? extends AuthDAO>> provideClasses() {
        return List.of(MemoryAuthDAO.class, DatabaseAuthDAO.class);
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void insertAuth(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        authDAO.clear();
        var authData = new AuthData("token", "username");
        authDAO.insertAuth(authData);
        assertEquals(authData, authDAO.getAuth(authData.authToken()));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void getNullAuth(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        authDAO.clear();
        var authData = new AuthData("token", "username");
        assertNull(authDAO.getAuth(authData.authToken()));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void deleteAuth(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        authDAO.clear();
        var authData = new AuthData("token", "username");
        authDAO.insertAuth(authData);
        authDAO.deleteAuth(authData.authToken());
        assertNull(authDAO.getAuth(authData.authToken()));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void deleteAuthTwice(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        authDAO.clear();
        var authData = new AuthData("token", "username");
        authDAO.insertAuth(authData);
        authDAO.deleteAuth(authData.authToken());
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth(authData.authToken()));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void clear(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        String[] usernames = {"user1", "user2", "user3"};
        String[] tokens = {"pw1", "pw2", "pw3"};
        AuthData[] auths = new AuthData[3];
        for (var i = 0; i < 3; i++) {
            auths[i] = new AuthData(tokens[i], usernames[i]);
            authDAO.insertAuth(auths[i]);
        }
        authDAO.clear();
        for (var i = 0; i < 3; i++) {
            assertNull(authDAO.getAuth(auths[i].authToken()));
        }
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void validAuthIsValid(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        authDAO.clear();
        var authData = new AuthData("token", "username");
        authDAO.insertAuth(authData);
        assertTrue(authDAO.authIsValid(authData));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void invalidAuthIsInvalid(Class<? extends AuthDAO> authDAOClass) throws Exception {
        var authDAO = authDAOClass.getDeclaredConstructor().newInstance();
        authDAO.clear();
        var authData = new AuthData("token", "username");
        assertFalse(authDAO.authIsValid(authData));
    }
}