package dataaccess;

import models.AuthData;

public interface AuthDAO {

    AuthData getAuth(String authToken);

    void insertAuth(AuthData authData);

    void deleteAuth(String authToken) throws DataAccessException;

    void clear();

    boolean authIsValid(AuthData auth2);
}
