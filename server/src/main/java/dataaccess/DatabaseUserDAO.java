package dataaccess;

import models.UserData;

public class DatabaseUserDAO implements UserDAO{
    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void insertUser(UserData userData) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}
