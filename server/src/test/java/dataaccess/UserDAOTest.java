package dataaccess;

import models.AuthData;
import models.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    @ParameterizedTest
    @ValueSource(classes = {MemoryUserDAO.class})
    void insertUser(Class<? extends UserDAO> userDAOClass) throws Exception {
        var userDAO = userDAOClass.getDeclaredConstructor().newInstance();
        var userData = new UserData("user", "pass", "k@k.com");
        userDAO.insertUser(userData);
        assertEquals(userData, userDAO.getUser(userData.username()));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemoryUserDAO.class})
    void insertDuplicateUsername(Class<? extends UserDAO> userDAOClass) throws Exception {
        var userDAO = userDAOClass.getDeclaredConstructor().newInstance();
        var userData = new UserData("user", "pass", "k@k.com");
        userDAO.insertUser(userData);
        var userData2 = new UserData("user", "pass2", "k2@k.com");
        assertThrows(DataAccessException.class, () -> userDAO.insertUser(userData2));
    }

    @Test
    void clear() {
    }
}