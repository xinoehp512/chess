package dataaccess;

import models.UserData;


public interface UserDAO {
    UserData getUser(String username);

    void insertUser(UserData userData) throws DataAccessException;

    void clear();
}
