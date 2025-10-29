package dataaccess;

import models.UserData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private static List<Class<? extends UserDAO>> provideClasses() {
        return List.of(MemoryUserDAO.class, DatabaseUserDAO.class);
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void insertUser(Class<? extends UserDAO> userDAOClass) throws Exception {
        var userDAO = userDAOClass.getDeclaredConstructor().newInstance();
        var userData = new UserData("user", "pass", "k@k.com");
        userDAO.insertUser(userData);
        assertEquals(userData, userDAO.getUser(userData.username()));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void insertDuplicateUsername(Class<? extends UserDAO> userDAOClass) throws Exception {
        var userDAO = userDAOClass.getDeclaredConstructor().newInstance();
        var userData = new UserData("user", "pass", "k@k.com");
        userDAO.insertUser(userData);
        var userData2 = new UserData("user", "pass2", "k2@k.com");
        assertThrows(DataAccessException.class, () -> userDAO.insertUser(userData2));
    }

    @ParameterizedTest
    @MethodSource("provideClasses")
    void clear(Class<? extends UserDAO> userDAOClass) throws Exception {
        var userDAO = userDAOClass.getDeclaredConstructor().newInstance();
        String[] usernames = {"user1", "user2", "user3"};
        String[] passwords = {"pw1", "pw2", "pw3"};
        String[] emails = {"em1@e.com", "em2@e.com", "em3@e.com"};
        for (var i = 0; i < 3; i++) {
            userDAO.insertUser(new UserData(usernames[i], passwords[i], emails[i]));
        }
        userDAO.clear();
        for (var i = 0; i < 3; i++) {
            assertNull(userDAO.getUser(usernames[i]));
        }
    }
}