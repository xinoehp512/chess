package dataaccess;

import models.AuthData;

public interface AuthDAO {

    AuthData getAuth(String authToken) throws DataAccessException;

    void insertAuth(AuthData authData) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    void clear();

    boolean authIsValid(AuthData authData);
}
