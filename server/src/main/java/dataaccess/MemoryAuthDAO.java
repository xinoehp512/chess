package dataaccess;

import models.AuthData;

import java.util.*;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String,AuthData> table = new HashMap<>();
    public AuthData getAuth(String authToken) {
        return table.get(authToken);
    }
    public void insertAuth(AuthData authData) {
        table.put(authData.authToken(), authData);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        if (table.remove(authToken) == null) {
            throw new DataAccessException("Auth Token is bad!");
        }
    }

    public void clear() {
        table.clear();
    }

    @Override
    public boolean authIsValid(AuthData authData) {
        return Objects.equals(table.get(authData.authToken()), authData);
    }
}
