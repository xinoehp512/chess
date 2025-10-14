package dataaccess;

import models.UserData;


public interface UserDAO {
    public UserData getUser(String username);

    public void insertUser(UserData userData) throws DataAccessException;

    public void clear();
}
