package dataaccess;

import models.UserData;


public interface UserDAO {
    UserData getUser(String username) throws DataAccessException;

    void insertUser(UserData userData) throws DataAccessException;

    void clear() throws DataAccessException;
}
