package dataaccess;

import models.AuthData;

public class DatabaseAuthDAO implements AuthDAO {

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void insertAuth(AuthData authData) {

    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() {

    }

    @Override
    public boolean authIsValid(AuthData authData) {
        return false;
    }
}
